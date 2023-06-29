package me.hydos.alchemytools.renderer.wrapper.image;

import me.hydos.alchemytools.renderer.wrapper.core.VkUtils;
import me.hydos.alchemytools.renderer.wrapper.core.VkWrapper;
import me.hydos.alchemytools.renderer.wrapper.init.Device;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkImageCreateInfo;

import java.util.function.Function;

import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_AUTO;
import static org.lwjgl.vulkan.VK11.*;

public class Image implements VkWrapper<Long> {

    public final Device device;
    public final int format;
    public final int mipLevels;
    private final long image;
    public final long allocation;

    private Image(Device device, Builder builder) {
        try (var stack = MemoryStack.stackPush()) {
            this.device = device;
            this.format = builder.format;
            this.mipLevels = builder.mipLevels;
            var allocator = builder.vmaAllocator == 0 ? device.memoryAllocator.vma() : builder.vmaAllocator;

            var imageCreateInfo = VkImageCreateInfo.calloc(stack)
                    .sType$Default()
                    .imageType(VK_IMAGE_TYPE_2D)
                    .format(this.format)
                    .extent(it -> it
                            .width(builder.width)
                            .height(builder.height)
                            .depth(1)
                    )
                    .mipLevels(this.mipLevels)
                    .arrayLayers(builder.arrayLayers)
                    .samples(builder.sampleCount)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .tiling(VK_IMAGE_TILING_OPTIMAL)
                    .usage(builder.usage)
                    .pNext(builder.pNext.apply(stack));

            var pImage = stack.mallocLong(1);
            var pAlloc = stack.mallocPointer(1);

            var createInfo = VmaAllocationCreateInfo.calloc(stack)
                    .requiredFlags(builder.properties)
                    .usage(VMA_MEMORY_USAGE_AUTO);

            VkUtils.ok(Vma.vmaCreateImage(allocator, imageCreateInfo, createInfo, pImage, pAlloc, null), "Failed to create image");
            this.image = pImage.get(0);
            this.allocation = pAlloc.get(0);
        }
    }

    @Override
    public void close() { // TODO: make allocation record which is long and VmaAllocator
        Vma.vmaDestroyImage(device.memoryAllocator.vma(), image, allocation);
    }

    @Override
    public Long vk() {
        return this.image;
    }

    public static class Builder {
        private int arrayLayers;
        private int format;
        private int height;
        private int mipLevels;
        private int sampleCount;
        private int usage;
        private int width;
        private int properties;
        private Function<MemoryStack, Long> pNext = stack -> 0L;
        private long vmaAllocator;
        private int imageType = VK10.VK_IMAGE_TYPE_2D;

        public Builder() {
            this.format = VK_FORMAT_R8G8B8A8_SRGB;
            this.mipLevels = 1;
            this.sampleCount = 1;
            this.arrayLayers = 1;
        }

        public Builder arrayLayers(int arrayLayers) {
            this.arrayLayers = arrayLayers;
            return this;
        }

        public Builder format(int format) {
            this.format = format;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder mipLevels(int mipLevels) {
            this.mipLevels = mipLevels;
            return this;
        }

        public Builder sampleCount(int sampleCount) {
            this.sampleCount = sampleCount;
            return this;
        }

        public Builder imageType(int imageType) {
            this.imageType = imageType;
            return this;
        }

        public Builder usage(int usage) {
            this.usage = usage;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder properties(int properties) {
            this.properties = properties;
            return this;
        }

        public Builder allocator(long vmaAllocator) {
            this.vmaAllocator = vmaAllocator;
            return this;
        }

        public Builder pNext(Function<MemoryStack, Long> pNext) {
            this.pNext = pNext;
            return this;
        }

        public Image build(Device device) {
            return new Image(device, this);
        }
    }
}