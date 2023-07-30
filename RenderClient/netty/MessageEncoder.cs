using DotNetty.Buffers;
using DotNetty.Codecs;
using DotNetty.Transport.Channels;

namespace RenderClient.netty; 

public class MessageEncoder : MessageToByteEncoder<ChannelMessage> {

    protected override void Encode(IChannelHandlerContext context, ChannelMessage msg, IByteBuffer output) {
        output.WriteShort(msg.PacketId);
        output.WriteByte(msg.Direction);
        output.WriteInt(msg.PacketData.Length);
        output.WriteBytes(msg.PacketData);
    }
}