package me.hydos.alchemytools.renderer.impl;

import me.hydos.alchemytools.renderer.scene.RenderEntity;

import java.util.ArrayList;
import java.util.List;

public class AnimatedEntity {

    public final RenderEntity entity;
    public final List<VulkanAnimMesh> meshes;
    public final GpuModel model;

    public AnimatedEntity(RenderEntity entity, GpuModel model) {
        this.entity = entity;
        this.model = model;
        this.meshes = new ArrayList<>();
    }

    public record VulkanAnimMesh(
            int meshOffset,
            GpuModel.VulkanMesh vulkanMesh
    ) {}
}
