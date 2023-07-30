package me.hydos.alchemytools.network.packets;

import me.hydos.alchemytools.network.netty.NetworkBuffer;
import me.hydos.alchemytools.network.RenderThread;

public class C2SPingPacket implements Packet {

    public final long delay;

    public C2SPingPacket(NetworkBuffer buf) {
        var writeTime = buf.readLong();
        this.delay = Math.max(0, System.currentTimeMillis() - writeTime);
        System.out.println("Took " + delay + "ms to send packet");
    }

    @Override
    public void handle(RenderThread renderThread) {
        // TODO: update RenderThread on how network is going
    }
}
