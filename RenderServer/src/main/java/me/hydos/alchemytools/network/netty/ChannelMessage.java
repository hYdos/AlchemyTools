package me.hydos.alchemytools.network.netty;

public record ChannelMessage(
        int packetId,
        byte direction,
        byte[] packetData
) {}
