package me.hydos.alchemytools.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<ChannelMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ChannelMessage msg, ByteBuf out) {
        out.writeShort(msg.packetId());
        out.writeByte(msg.direction());
        out.writeBytes(msg.packetData());
    }
}
