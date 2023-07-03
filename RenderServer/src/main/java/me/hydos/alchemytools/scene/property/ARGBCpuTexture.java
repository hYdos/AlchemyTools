package me.hydos.alchemytools.scene.property;

import java.nio.ByteBuffer;

public record ARGBCpuTexture(
        ByteBuffer data,
        int width,
        int height
) {}
