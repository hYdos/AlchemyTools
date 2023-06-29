package me.hydos.alchemytools.util.model;

import me.hydos.alchemytools.renderer.scene.ModelData;
import me.hydos.alchemytools.scene.ARGBCpuTexture;
import me.hydos.alchemytools.util.model.animation.Skeleton;

@Deprecated(forRemoval = true) //TODO: replace with new Scene system
public record Model(
        String name,
        ModelData.Material[] materials,
        ARGBCpuTexture[] textures,
        Mesh[] meshes,
        Skeleton skeleton
) {}
