namespace RenderClient.netty;

public struct ChannelMessage {
    public readonly int PacketId;
    public readonly byte Direction;
    public readonly byte[] PacketData;

    public ChannelMessage(int packetId, byte direction, byte[] packetData) {
        PacketId = packetId;
        Direction = direction;
        PacketData = packetData;
    }
}