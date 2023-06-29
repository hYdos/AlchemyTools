package me.hydos.alchemytools.renderer.impl.lighting;

import me.hydos.alchemytools.renderer.wrapper.descriptor.DescriptorPool;
import me.hydos.alchemytools.renderer.wrapper.descriptor.DescriptorSet;
import me.hydos.alchemytools.renderer.wrapper.image.TextureSampler;
import me.hydos.alchemytools.renderer.wrapper.init.Device;
import me.hydos.alchemytools.renderer.wrapper.renderpass.Attachment;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.List;

import static me.hydos.alchemytools.renderer.wrapper.core.VkUtils.ok;
import static org.lwjgl.vulkan.VK11.*;

public class AttachmentsDescriptorSet extends DescriptorSet {

    private final int binding;
    private final Device device;
    private final TextureSampler textureSampler;

    public AttachmentsDescriptorSet(DescriptorPool descriptorPool, AttachmentsLayout descriptorSetLayout,
                                    List<Attachment> attachments, int binding) {
        try (var stack = MemoryStack.stackPush()) {
            this.device = descriptorPool.getDevice();
            this.binding = binding;
            var pDescriptorSetLayout = stack.mallocLong(1);
            pDescriptorSetLayout.put(0, descriptorSetLayout.vk());
            var allocInfo = VkDescriptorSetAllocateInfo.calloc(stack)
                    .sType$Default()
                    .descriptorPool(descriptorPool.vk())
                    .pSetLayouts(pDescriptorSetLayout);

            var pDescriptorSet = stack.mallocLong(1);
            ok(vkAllocateDescriptorSets(this.device.vk(), allocInfo, pDescriptorSet),
                    "Failed to create descriptor set");

            this.vkDescriptorSet = pDescriptorSet.get(0);

            this.textureSampler = new TextureSampler(this.device, 1);

            update(attachments);
        }
    }

    public void close() {
        this.textureSampler.close();
    }

    public void update(List<Attachment> attachments) {
        try (var stack = MemoryStack.stackPush()) {
            var numAttachments = attachments.size();
            var descrBuffer = VkWriteDescriptorSet.calloc(numAttachments, stack);
            for (var i = 0; i < numAttachments; i++) {
                var attachment = attachments.get(i);
                var imageInfo = VkDescriptorImageInfo.calloc(1, stack)
                        .sampler(this.textureSampler.getVkSampler())
                        .imageView(attachment.getImageView().vk());
                if (attachment.isDepthAttachment())
                    imageInfo.imageLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL);
                else imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

                descrBuffer.get(i)
                        .sType$Default()
                        .dstSet(this.vkDescriptorSet)
                        .dstBinding(this.binding + i)
                        .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                        .descriptorCount(1)
                        .pImageInfo(imageInfo);
            }

            vkUpdateDescriptorSets(this.device.vk(), descrBuffer, null);
        }
    }
}