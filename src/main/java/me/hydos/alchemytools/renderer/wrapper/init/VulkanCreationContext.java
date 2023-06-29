package me.hydos.alchemytools.renderer.wrapper.init;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_MAKE_VERSION;

public class VulkanCreationContext {

    public final List<String> instanceExtensions = new ArrayList<>();
    public final List<String> deviceExtensions = new ArrayList<>();
    public final List<String> enabledFeatures = new ArrayList<>();
    public final String appName;
    public boolean enableSharedMemAlloc;
    public String engineName = "???";
    public int engineVersion = VK_MAKE_VERSION(0, 1, 0);

    public VulkanCreationContext(String appName) {
        this.appName = appName;
    }

    public VulkanCreationContext instanceExtension(String extensionName) {
        this.instanceExtensions.add(extensionName);
        return this;
    }

    public VulkanCreationContext deviceExtension(String extensionName) {
        deviceExtensions.add(extensionName);
        return this;
    }

    public VulkanCreationContext enableFeature(String feature) {
        enabledFeatures.add(feature);
        return this;
    }

    public VulkanCreationContext engine(String name, int major, int minor, int patch) {
        this.engineName = name;
        this.engineVersion = VK_MAKE_VERSION(major, minor, patch);
        return this;
    }

    //FIXME: bad
    public VulkanCreationContext sharedAllocator(boolean sharedAllocator) {
        this.enableSharedMemAlloc = sharedAllocator;
        return this;
    }
}
