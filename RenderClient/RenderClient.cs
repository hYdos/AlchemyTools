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
    private bool _close;
    private IChannel? _channel;

    static RenderClient() {
        C2SPackets.Add(typeof(C2SInitRenderer));
    }

    public RenderClient(int port) {
        _workerGroup = new MultithreadEventLoopGroup();
        _port = port;
    }

    public void SendPacket(Packet packet) {
        NetworkQueue.Enqueue(() => { _channel?.WriteAndFlushAsync(packet); });
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
            .Option(ChannelOption.TcpNodelay, true);

        _channel = await b.ConnectAsync("localhost", _port);

        while (!_close) {
            ProcessNetworkQueue();
        }

        _close = true;
    }

    private static void ProcessNetworkQueue() {
        while (NetworkQueue.Count > 0) {
            var action = NetworkQueue.Dequeue();
            action();
        }
    }

    public async void Dispose() {
        _close = true;
        await _channel?.CloseAsync()!;
        await _workerGroup.ShutdownGracefullyAsync();
    }
}