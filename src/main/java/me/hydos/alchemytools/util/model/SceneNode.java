package me.hydos.alchemytools.util.model;

import me.hydos.alchemytools.renderer.scene.ModelData;
import me.hydos.alchemytools.scene.ARGBCpuTexture;
import me.hydos.alchemytools.util.model.animation.Skeleton;
import org.joml.Matrix4f;

@Deprecated(forRemoval = true) //TODO: replace with new Scene system
public record SceneNode(
        String name,
        Matrix4f transform,
        ModelData.Material[] materials,
        ARGBCpuTexture[] textures,
        Mesh[] meshes,
        Skeleton skeleton
) {}
