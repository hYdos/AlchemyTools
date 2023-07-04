using DotNetty.Transport.Bootstrapping;
using DotNetty.Transport.Channels;
using DotNetty.Transport.Channels.Sockets;
using RenderClient.netty;
using RenderClient.packets.init;

namespace RenderClient;

/**
 * C# Side of the Java RenderServer
 */
public class RenderClient : IDisposable {
    private static readonly List<Type> C2SPackets = new();
    private static readonly List<Packet> S2CPackets = new();
    private static readonly Queue<Action> NetworkQueue = new();
    private readonly MultithreadEventLoopGroup _workerGroup;
    private readonly int _port;
    private readonly Thread _networkThread;
    private bool _close;
    private IChannel? _channel;

    static RenderClient() {
        C2SPackets.Add(typeof(C2SInitRenderer));
    }

    public RenderClient(int port) {
        _workerGroup = new MultithreadEventLoopGroup();
        _port = port;
        _networkThread = new Thread(NetworkThreadWorker);
        _networkThread.Start();
    }

    public void SendPacket(Packet packet) {
        NetworkQueue.Enqueue(() => {
            _channel.WriteAndFlushAsync(packet);
            _channel.Flush();
        });
    }

    private async void NetworkThreadWorker() {
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
            .Option(ChannelOption.TcpNodelay, true);

        _channel = await b.ConnectAsync("localhost", _port);

        while (!_close) {
            while (NetworkQueue.Count > 0) NetworkQueue.Dequeue()();
        }

        _close = true;
    }

    public async void Dispose() {
        _close = true;
        await _channel.CloseAsync();
        await _workerGroup.ShutdownGracefullyAsync();
        _networkThread.Join();
    }
}