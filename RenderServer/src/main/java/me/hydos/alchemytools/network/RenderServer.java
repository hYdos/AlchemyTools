package me.hydos.alchemytools.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.hydos.alchemytools.network.netty.MessageDecoder;
import me.hydos.alchemytools.network.netty.MessageEncoder;
import me.hydos.alchemytools.network.netty.NetworkBuffer;
import me.hydos.alchemytools.network.netty.PacketProcessor;
import me.hydos.alchemytools.network.packets.C2SInitRenderer;
import me.hydos.alchemytools.network.packets.C2SPingPacket;
import me.hydos.alchemytools.network.packets.Packet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RenderServer extends Thread implements Closeable {
    public static final Logger LOGGER = LoggerFactory.getLogger("Render Server");
    public static final List<Function<NetworkBuffer, ? extends Packet>> C2S_PACKETS = new ArrayList<>();
    public static final List<Class<? extends Packet>> S2C_PACKETS = new ArrayList<>();
    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;
    private final int port;

    public RenderServer(int port) {
        super("Network Thread");
        LOGGER.info("Netty server running on port {}", port);
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.port = port;
    }

    @Override
    public void run() {
        try {
            var b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(@NotNull SocketChannel ch) {
                            ch.pipeline().addLast(new MessageDecoder(), new MessageEncoder(), new PacketProcessor());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            var future = b.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean shouldEnd() {
        return false;
    }

    @Override
    public void close() throws IOException {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    static {
        C2S_PACKETS.add(C2SInitRenderer::new);
        C2S_PACKETS.add(C2SPingPacket::new);
    }
}
