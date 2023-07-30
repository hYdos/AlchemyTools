using System.Net;
using System.Threading.Channels;
using DotNetty.Transport.Bootstrapping;
using DotNetty.Transport.Channels;
using DotNetty.Transport.Channels.Sockets;
using RenderClient.netty;
using RenderClient.packets;

namespace RenderClient;

/**
 * C# Side of the Java RenderServer
 */
public class RenderClient : IDisposable {
    private static readonly List<Type> C2SPackets = new();
    private static readonly List<Packet> S2CPackets = new();
    private readonly Channel<Packet> NetworkQueue = Channel.CreateUnbounded<Packet>();
    private readonly MultithreadEventLoopGroup _workerGroup;
    private readonly int _port;
    private volatile bool _close;
    private IChannel? _channel;

    static RenderClient() {
        C2SPackets.Add(typeof(C2SInitRenderer));
        C2SPackets.Add(typeof(C2SPingPacket));
    }

    public RenderClient(int port) {
        _workerGroup = new MultithreadEventLoopGroup();
        _port = port;
    }

    public void SendPacket(Packet packet) {
        NetworkQueue.Writer.TryWrite(packet);
    }

    public async Task Start() {
        var b = new Bootstrap();
        b.Group(_workerGroup)
            .Channel<TcpSocketChannel>()
            .Handler(new ActionChannelInitializer<ISocketChannel>(ch => {
                ch.Pipeline.AddLast(
                    new MessageDecoder(),
                    new MessageEncoder(),
                    new PacketProcessor(),
                    new ExceptionHandler());
            }))
            .Option(ChannelOption.SoKeepalive, true)
            .Option(ChannelOption.TcpNodelay, true);

        _channel = await b.ConnectAsync(new IPEndPoint(IPAddress.Loopback, _port));

        try {
            while (!_close) {
                await ProcessNetworkQueue();
            }
        }
        catch (ChannelClosedException) {
            //ignored
        }

        _close = true;
    }

    private async Task ProcessNetworkQueue() {
        var packet = await NetworkQueue.Reader.ReadAsync();

        using var stream = new MemoryStream();
        await using (var writer = new BinaryWriter(stream)) {
            packet.Write(writer);
        }

        await _channel!.WriteAndFlushAsync(new ChannelMessage(C2SPackets.IndexOf(packet.GetType()), 0, stream.ToArray()));
    }

    public async void Dispose() {
        _close = true;
        NetworkQueue.Writer.Complete();
        if (_channel != null)
            await _channel.CloseAsync();

        await _workerGroup.ShutdownGracefullyAsync();
    }
}