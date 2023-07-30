package me.hydos.alchemytools.network.packets;

import me.hydos.alchemytools.network.netty.NetworkBuffer;
import me.hydos.alchemytools.network.RenderThread;

public interface Packet {

    /**
     * Called inside the network thread and should be quick conversion of the data to bytes for sending to the client
     */
    default void write(NetworkBuffer buf) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Called outside the Network Thread for handling incoming packets from the client
     */
    void handle(RenderThread renderThread);
}
