package me.hydos.alchemytools.renderer.impl;

import me.hydos.alchemytools.io.Display;
import me.hydos.alchemytools.renderer.impl.animation.GpuAnimator;
import me.hydos.alchemytools.renderer.impl.geometry.GeometryPass;
import me.hydos.alchemytools.renderer.impl.lighting.LightPass;
import me.hydos.alchemytools.renderer.impl.shadows.ShadowPass;
import me.hydos.alchemytools.renderer.scene.ModelData;
import me.hydos.alchemytools.renderer.scene.Scene;
import me.hydos.alchemytools.renderer.wrapper.cmd.CmdBuffer;
import me.hydos.alchemytools.renderer.wrapper.cmd.CmdPool;
import me.hydos.alchemytools.renderer.wrapper.core.Fence;
import me.hydos.alchemytools.renderer.wrapper.core.Queue;
import me.hydos.alchemytools.renderer.wrapper.core.Configuration;
import me.hydos.alchemytools.renderer.wrapper.core.Swapchain;
import me.hydos.alchemytools.renderer.wrapper.init.Device;
import me.hydos.alchemytools.renderer.wrapper.init.VulkanCreationContext;
import me.hydos.alchemytools.renderer.wrapper.init.Instance;
import me.hydos.alchemytools.renderer.wrapper.init.PhysicalDevice;
import me.hydos.alchemytools.renderer.wrapper.pipeline.PipelineCache;
import me.hydos.alchemytools.renderer.wrapper.window.Surface;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.vulkan.VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;

public class Renderer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Renderer.class);
    private final GpuAnimator computeAnimator;
    private final GeometryPass geometryPass;
    private final LightPass lightPass;
    private final ShadowPass shadowPass;
    public final CmdPool cmdPool;

    public final Device device;
    public final GlobalBuffers globalBuffers;
    public final Queue.GraphicsQueue graphicsQueue;
    public final Instance instance;
    public final PhysicalDevice physicalDevice;
    public final PipelineCache pipelineCache;
    public final Queue.PresentQueue presentQueue;
    public final Surface surface;
    public final TextureCache textureCache;
    public final List<GpuModel> gpuModels;
    public CmdBuffer[] cmdBuffers;
    public long entityLoadTime;
    public Fence[] fences;
    public Swapchain swapchain;

    public Renderer(@NotNull Display display, VulkanCreationContext provider, Scene scene) {
        var settings = Configuration.getInstance();
        this.instance = new Instance(settings.debug, true, provider);
        this.physicalDevice = PhysicalDevice.createPhysicalDevice(this.instance);
        this.device = new Device(this.instance, this.physicalDevice, provider);
        this.surface = new Surface(this.physicalDevice, display.handle());
        this.graphicsQueue = new Queue.GraphicsQueue(this.device, 0);
        this.presentQueue = new Queue.PresentQueue(this.device, this.surface, 0);
        this.swapchain = new Swapchain(this.device, this.surface, display, settings.swapchainImgCount, settings.enableVsync);
        this.cmdPool = new CmdPool(this.device, this.graphicsQueue.getQueueFamilyIndex());
        this.pipelineCache = new PipelineCache(this.device);
        this.gpuModels = new ArrayList<>();
        this.textureCache = new TextureCache();
        this.globalBuffers = new GlobalBuffers(this.device);
        this.geometryPass = new GeometryPass(this.swapchain, this.pipelineCache, scene, this.globalBuffers);
        this.shadowPass = new ShadowPass(this.swapchain, this.pipelineCache, scene);
        var attachments = new ArrayList<>(this.geometryPass.getAttachments());
        attachments.add(this.shadowPass.getDepthAttachment());
        this.lightPass = new LightPass(this.swapchain, this.cmdPool, this.pipelineCache, attachments, scene);
        this.computeAnimator = new GpuAnimator(this.cmdPool, this.pipelineCache);
        this.entityLoadTime = 0;
        createCommandBuffers();
    }

    private CmdBuffer acquireCurrentCommandBuffer() {
        var idx = this.swapchain.getCurrentFrame();
        var fence = this.fences[idx];
        var commandBuffer = this.cmdBuffers[idx];

        fence.waitForFence();
        fence.reset();
        return commandBuffer;
    }

    public void close() {
        this.presentQueue.waitIdle();
        this.graphicsQueue.waitIdle();
        this.device.waitIdle();
        this.textureCache.close();
        this.pipelineCache.close();
        this.lightPass.close();
        this.computeAnimator.close();
        this.shadowPass.close();
        this.geometryPass.close();
        Arrays.stream(this.cmdBuffers).forEach(CmdBuffer::close);
        Arrays.stream(this.fences).forEach(Fence::close);
        this.cmdPool.close();
        this.swapchain.close();
        this.surface.close();
        this.globalBuffers.close();
        this.device.close();
        this.physicalDevice.close();
        this.instance.close();
    }

    private void createCommandBuffers() {
        var imageCount = swapchain.getImageCount();
        this.cmdBuffers = new CmdBuffer[imageCount];
        this.fences = new Fence[imageCount];

        for (var i = 0; i < imageCount; i++) {
            this.cmdBuffers[i] = cmdPool.newBuffer(true, false);
            this.fences[i] = new Fence(this.device, true);
        }
    }

    @Deprecated // FIXME: make this work more proper
    public void loadModels(List<ModelData> models) {
        LOGGER.info("Loading {} model(s)", models.size());
        this.gpuModels.addAll(this.globalBuffers.loadModels(models, this.textureCache, this.cmdPool, this.graphicsQueue));
        LOGGER.info("Loaded {} model(s)", models.size());

        this.geometryPass.loadModels(this.textureCache);
    }

    private void recordCommands() {
        var idx = 0;
        for (var cmdBuffer : this.cmdBuffers) {
            var currentIdx = idx;
            cmdBuffer.reset();
            cmdBuffer.record(null, false, () -> {
                this.geometryPass.recordCommandBuffer(cmdBuffer, this.globalBuffers, currentIdx);
                this.shadowPass.recordCommandBuffer(cmdBuffer, this.globalBuffers, currentIdx);
                return null;
            });
            idx++;
        }
    }

    public void render(Display display, Scene scene) {
        if (this.entityLoadTime < scene.lastEntityLoadTime()) {
            this.entityLoadTime = scene.lastEntityLoadTime();
            this.device.waitIdle();
            this.globalBuffers.loadEntities(this.gpuModels, scene, this.cmdPool, this.graphicsQueue, this.swapchain.getImageCount());
            this.computeAnimator.onAnimatedEntitiesLoaded(this.globalBuffers);
            recordCommands();
        }
        if (display.getWidth() <= 0 && display.getHeight() <= 0) return;
        if (display.isResized() || this.swapchain.acquireNextImage()) {
            display.resetResized();
            resize(display);
            scene.getProjection().resize(display.getWidth(), display.getHeight());
            this.swapchain.acquireNextImage();
        }

        this.globalBuffers.loadInstanceData(scene, this.gpuModels, this.swapchain.getCurrentFrame());

        this.computeAnimator.recordCommandBuffer(this.globalBuffers);
        this.computeAnimator.submit();

        var commandBuffer = acquireCurrentCommandBuffer();
        this.geometryPass.render();
        this.shadowPass.render();
        submitSceneCommand(this.graphicsQueue, commandBuffer);

        commandBuffer = this.lightPass.beginRecording(this.shadowPass.getShadowCascades());
        this.lightPass.recordCommandBuffer(commandBuffer);
        this.lightPass.endRecording(commandBuffer);
        this.lightPass.submit(this.graphicsQueue);

        if (this.swapchain.presentImage(this.graphicsQueue)) display.setResized(true);
    }

    private void resize(Display display) {
        var settings = Configuration.getInstance();
        this.device.waitIdle();
        this.graphicsQueue.waitIdle();

        this.swapchain.close();
        this.swapchain = new Swapchain(this.device, this.surface, display, settings.swapchainImgCount, settings.enableVsync);
        this.geometryPass.resize(this.swapchain);
        this.shadowPass.resize(this.swapchain);
        recordCommands();
        var attachments = new ArrayList<>(this.geometryPass.getAttachments());
        attachments.add(this.shadowPass.getDepthAttachment());
        this.lightPass.resize(this.swapchain, attachments);
    }

    public void submitSceneCommand(Queue queue, CmdBuffer cmdBuffer) {
        try (var stack = MemoryStack.stackPush()) {
            var idx = this.swapchain.getCurrentFrame();
            var currentFence = this.fences[idx];
            var syncSemaphores = this.swapchain.getSyncSemaphoresList()[idx];
            queue.submit(stack.pointers(cmdBuffer.vk()),
                    stack.longs(syncSemaphores.imgAcquisitionSemaphore().getVkSemaphore()),
                    stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT),
                    stack.longs(syncSemaphores.geometryCompleteSemaphore().getVkSemaphore()), currentFence);
        }
    }
}