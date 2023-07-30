package me.hydos.alchemytools.network.netty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NetworkBuffer {

    private final ByteBuffer internalBuffer;

    public NetworkBuffer(ByteBuffer internalBuffer) {
        this.internalBuffer = internalBuffer;
        internalBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte readByte() {
        return (byte) Byte.toUnsignedInt(internalBuffer.get());
    }

    public int readInt() {
        return internalBuffer.getInt();
    }

    public int readInt(int idx) {
        return internalBuffer.getInt(idx);
    }

    public short readShort() {
        return internalBuffer.getShort();
    }

    public char readChar() {
        return (char) internalBuffer.get();
    }

    public long readLong() {
        return internalBuffer.getLong();
    }

    public String readString() {
        var length = readInt();
        var string = new StringBuilder();
        for (int i = 0; i < length; i++) string.append(readChar());
        return string.toString();
    }

    public ByteBuffer buf() {
        return internalBuffer;
    }
}
