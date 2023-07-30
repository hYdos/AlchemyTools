package me.hydos.alchemytools.network.packets;

import me.hydos.alchemytools.network.C2SHandler;
import me.hydos.alchemytools.network.netty.NetworkBuffer;
import me.hydos.alchemytools.network.RenderThread;

public class C2SInitRenderer implements Packet {

    public final int width;
    public final int height;
    public final String appName;
    public final String title;

    public C2SInitRenderer(NetworkBuffer buf) {
        this.width = buf.readInt();
        this.height = buf.readInt();
        this.appName = buf.readString();
        this.title = buf.readString();
    }

    @Override
    public void handle(RenderThread renderThread) {
        C2SHandler.onInitRenderer(this, renderThread);
    }
}
