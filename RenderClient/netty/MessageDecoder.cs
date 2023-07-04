using DotNetty.Buffers;
using DotNetty.Codecs;
using DotNetty.Transport.Channels;

namespace RenderClient.netty;

public class MessageDecoder : ReplayingDecoder<ChannelMessage> {

    public MessageDecoder() : base(new ChannelMessage()) {
    }

    protected override void Decode(IChannelHandlerContext context, IByteBuffer input, List<object> output) {
        var packetId = input.ReadShort();
        var direction = input.ReadByte();
        var packetBytes = new byte[input.Capacity - input.ReaderIndex];
        input.GetBytes(input.ReaderIndex, packetBytes);
        output.Add(new ChannelMessage(packetId, direction, packetBytes));
    }
}