package me.hydos.alchemytools.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class MessageDecoder extends ReplayingDecoder<ChannelMessage> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        var packetId = in.readShort();
        var direction = in.readByte();
        var packetBytes = new byte[in.capacity() - in.readerIndex()];
        in.getBytes(in.readerIndex(), packetBytes);
        out.add(new ChannelMessage(packetId, direction, packetBytes));
    }
}
