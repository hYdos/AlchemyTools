package me.hydos.alchemytools.scene;

import java.nio.ByteBuffer;

public record ARGBCpuTexture(
        ByteBuffer data,
        int width,
        int height
) {}
