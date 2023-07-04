using DotNetty.Transport.Channels;

namespace RenderClient.netty; 

public class PacketProcessor : ChannelHandlerAdapter {

    public override void ChannelRead(IChannelHandlerContext ctx, object msg) {
        var requestData = (ChannelMessage) msg;
        Console.WriteLine("oh no");
    }
}