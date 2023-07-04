using DotNetty.Transport.Channels;

namespace RenderClient.netty;

public class ExceptionHandler : ChannelHandlerAdapter {

    public override void ExceptionCaught(IChannelHandlerContext context, Exception exception) {
        Console.WriteLine($"Exception caught: {exception}");
        context.CloseAsync();
    }
}