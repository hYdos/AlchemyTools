package me.hydos.alchemytools.renderer.impl.lighting;

import me.hydos.alchemytools.renderer.wrapper.vertex.VertexInputStateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;

public class EmptyVertexBufferStructure extends VertexInputStateInfo {

    public EmptyVertexBufferStructure() {
        this.vi = VkPipelineVertexInputStateCreateInfo.calloc();
        this.vi.sType$Default()
                .pVertexBindingDescriptions(null)
                .pVertexAttributeDescriptions(null);
    }
}