package me.hydos.alchemytools.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import me.hydos.alchemytools.network.RenderServer;

import java.io.IOException;
import java.util.List;

public class MessageDecoder extends ReplayingDecoder<ChannelMessage> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            var packetId = in.readShort();
            var direction = in.readByte();
            var dataLength = in.readInt();
            var packetBytes = new byte[dataLength];
            for (int i = 0; i < packetBytes.length; i++) packetBytes[i] = in.readByte();
            out.add(new ChannelMessage(packetId, direction, packetBytes));
        } catch (Exception e) {
            RenderServer.LOGGER.error("Failed to decode packet :(");
            throw new RuntimeException(e);
        }
    }
}
