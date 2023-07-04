package me.hydos.alchemytools.network.packets;

import me.hydos.alchemytools.network.NetworkBuffer;
import me.hydos.alchemytools.network.RenderThread;

public class C2SInitRenderer implements Packet {

    public C2SInitRenderer(NetworkBuffer buf) {
    }

    @Override
    public void write(NetworkBuffer buf) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public void handle(RenderThread renderThread) {

    }
}
