package me.hydos.alchemytools.renderer.wrapper.vertex;

import me.hydos.alchemytools.renderer.wrapper.core.VkConstants;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

public class InstancedVertexBufferStructure extends VertexInputStateInfo {

    public static final int TEXT_COORD_COMPONENTS = 2;
    private static final int NORMAL_COMPONENTS = 3;
    private static final int NUMBER_OF_ATTRIBUTES = 10;
    private static final int POSITION_COMPONENTS = 3;
    public static final int SIZE_IN_BYTES = (POSITION_COMPONENTS + NORMAL_COMPONENTS * 3 + TEXT_COORD_COMPONENTS) * VkConstants.FLOAT_LENGTH;

    private final VkVertexInputAttributeDescription.Buffer viAttrs;
    private final VkVertexInputBindingDescription.Buffer viBindings;

    public InstancedVertexBufferStructure() {
        this.viAttrs = VkVertexInputAttributeDescription.calloc(NUMBER_OF_ATTRIBUTES);
        this.viBindings = VkVertexInputBindingDescription.calloc(2);
        this.vi = VkPipelineVertexInputStateCreateInfo.calloc();

        var i = 0;
        // Position
        this.viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(0);

        // Normal
        i++;
        this.viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(POSITION_COMPONENTS * VkConstants.FLOAT_LENGTH);

        // Tangent
        i++;
        this.viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(NORMAL_COMPONENTS * VkConstants.FLOAT_LENGTH + POSITION_COMPONENTS * VkConstants.FLOAT_LENGTH);

        // BiTangent
        i++;
        this.viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(NORMAL_COMPONENTS * VkConstants.FLOAT_LENGTH * 2 + POSITION_COMPONENTS * VkConstants.FLOAT_LENGTH);

        // Texture coordinates
        i++;
        this.viAttrs.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32_SFLOAT)
                .offset(NORMAL_COMPONENTS * VkConstants.FLOAT_LENGTH * 3 + POSITION_COMPONENTS * VkConstants.FLOAT_LENGTH);

        // Model Matrix as a set of 4 Vectors
        i++;
        for (var j = 0; j < 4; j++) {
            this.viAttrs.get(i)
                    .binding(1)
                    .location(i)
                    .format(VK_FORMAT_R32G32B32A32_SFLOAT)
                    .offset(j * VkConstants.VEC4_SIZE);
            i++;
        }
        this.viAttrs.get(i)
                .binding(1)
                .location(i)
                .format(VK_FORMAT_R8_UINT)
                .offset(VkConstants.VEC4_SIZE * 4);

        // Non instanced data
        this.viBindings.get(0)
                .binding(0)
                .stride(SIZE_IN_BYTES)
                .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        // Instanced data
        this.viBindings.get(1)
                .binding(1)
                .stride(VkConstants.MAT4X4_SIZE + VkConstants.INT_LENGTH)
                .inputRate(VK_VERTEX_INPUT_RATE_INSTANCE);

        this.vi
                .sType$Default()
                .pVertexBindingDescriptions(this.viBindings)
                .pVertexAttributeDescriptions(this.viAttrs);
    }

    @Override
    public void close() {
        super.close();
        this.viBindings.free();
        this.viAttrs.free();
    }
}