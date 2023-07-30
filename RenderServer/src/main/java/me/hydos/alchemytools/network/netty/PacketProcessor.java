package me.hydos.alchemytools.network.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.hydos.alchemytools.network.RenderServer;
import me.hydos.alchemytools.network.RenderThread;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class PacketProcessor extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        var requestData = (ChannelMessage) msg;
        var packetFactory = RenderServer.C2S_PACKETS.get(requestData.packetId());
        var packet = packetFactory.apply(new NetworkBuffer(ByteBuffer.wrap(requestData.packetData())));
        RenderThread.queue(packet::handle);
    }
}
