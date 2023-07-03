package me.hydos.alchemytools.renderer.wrapper.init;

import me.hydos.alchemytools.renderer.wrapper.core.VkUtils;
import me.hydos.alchemytools.renderer.wrapper.core.VkWrapper;
import me.hydos.alchemytools.renderer.wrapper.memory.MemoryAllocator;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.lwjgl.util.vma.Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT;
import static org.lwjgl.vulkan.VK11.*;

public class Device implements VkWrapper<VkDevice> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Device.class);
    public final MemoryAllocator memoryAllocator;
    public final MemoryAllocator sharedMemoryAllocator;
    private final PhysicalDevice physicalDevice;
    private final boolean samplerAnisotropy;
    private final VkDevice vkDevice;

    public Device(Instance instance, PhysicalDevice physicalDevice, VulkanCreationContext provider) {
        LOGGER.info("Creating device");

        this.physicalDevice = physicalDevice;
        try (var stack = MemoryStack.stackPush()) {

            // Define required extensions
            var requiredExtensions = new ArrayList<>(provider.deviceExtensions);
            requiredExtensions.add(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);

            var pRequiredExtensions = stack.mallocPointer(requiredExtensions.size());
            for (var i = 0; i < requiredExtensions.size(); i++) {
                LOGGER.info("Using Extension {}", requiredExtensions.get(i));
                pRequiredExtensions.put(i, stack.ASCII(requiredExtensions.get(i)));
            }

            // Set up required features
            var features = VkPhysicalDeviceFeatures.calloc(stack);
            var supportedFeatures = this.physicalDevice.getVkPhysicalDeviceFeatures();
            this.samplerAnisotropy = supportedFeatures.samplerAnisotropy();
            if (this.samplerAnisotropy) features.samplerAnisotropy(true);
            features.depthClamp(supportedFeatures.depthClamp());
            features.geometryShader(true);
            if (!supportedFeatures.multiDrawIndirect()) throw new RuntimeException("Multi draw Indirect not supported");
            features.multiDrawIndirect(true);

            // Enable all the queue families
            var queuePropsBuff = physicalDevice.getVkQueueFamilyProps();
            var numQueuesFamilies = queuePropsBuff.capacity();
            var queueCreationInfoBuf = VkDeviceQueueCreateInfo.calloc(numQueuesFamilies, stack);
            for (var i = 0; i < numQueuesFamilies; i++) {
                var priorities = stack.callocFloat(queuePropsBuff.get(i).queueCount());
                queueCreationInfoBuf.get(i)
                        .sType$Default()
                        .queueFamilyIndex(i)
                        .pQueuePriorities(priorities);
            }

            var deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType$Default()
                    .ppEnabledExtensionNames(pRequiredExtensions)
                    .pEnabledFeatures(features)
                    .pQueueCreateInfos(queueCreationInfoBuf);

            var chain = deviceCreateInfo.address();

            var unusedFeatures = new ArrayList<>(provider.enabledFeatures);
            for (var enabledFeature : provider.enabledFeatures)
                switch (enabledFeature) {
                    case "bufferDeviceAddress" -> {
                        var next = VkPhysicalDeviceBufferDeviceAddressFeaturesKHR.calloc(stack)
                                .sType$Default()
                                .bufferDeviceAddress(true);
                        MemoryUtil.memPutAddress(chain + 8, next.address()); // awful but + 8 guarantees pNext
                        chain = next.address();
                        unusedFeatures.remove(enabledFeature);
                    }
                }

            if (unusedFeatures.size() > 0)
                throw new RuntimeException("The following features dont exist: " + unusedFeatures);

            var pp = stack.mallocPointer(1);
            VkUtils.ok(vkCreateDevice(physicalDevice.vk(), deviceCreateInfo, null, pp),
                    "Failed to create device");
            this.vkDevice = new VkDevice(pp.get(0), physicalDevice.vk(), deviceCreateInfo);

            this.memoryAllocator = new MemoryAllocator(instance, physicalDevice, this.vkDevice, 0, false);
            this.sharedMemoryAllocator = new MemoryAllocator(instance, physicalDevice, this.vkDevice, VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT, true);
        }
    }

    @Override
    public void close() {
        LOGGER.info("Closing");
        this.memoryAllocator.close();
        vkDestroyDevice(this.vkDevice, null);
    }

    public MemoryAllocator getMemoryAllocator() {
        return this.memoryAllocator;
    }

    public PhysicalDevice getPhysicalDevice() {
        return this.physicalDevice;
    }

    @Override
    public VkDevice vk() {
        return this.vkDevice;
    }

    public boolean isSamplerAnisotropy() {
        return this.samplerAnisotropy;
    }

    public void waitIdle() {
        vkDeviceWaitIdle(this.vkDevice);
    }
}
