package me.hydos.alchemytools.renderer.impl.lighting;

import me.hydos.alchemytools.renderer.wrapper.core.Swapchain;
import me.hydos.alchemytools.renderer.wrapper.renderpass.FrameBuffer;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class LightingFrameBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LightingFrameBuffer.class);
    private final LightingRenderPass lightingRenderPass;

    private FrameBuffer[] frameBuffers;

    public LightingFrameBuffer(Swapchain swapChain) {
        LOGGER.info("Creating Lighting FrameBuffer");
        this.lightingRenderPass = new LightingRenderPass(swapChain);
        createFrameBuffers(swapChain);
    }

    public void close() {
        LOGGER.info("Closing");
        Arrays.stream(this.frameBuffers).forEach(FrameBuffer::close);
        this.lightingRenderPass.close();
    }

    private void createFrameBuffers(Swapchain swapChain) {
        try (var stack = MemoryStack.stackPush()) {
            var extent2D = swapChain.getSwapChainExtent();
            var width = extent2D.width();
            var height = extent2D.height();

            var numImages = swapChain.getImageCount();
            this.frameBuffers = new FrameBuffer[numImages];
            var attachmentsBuff = stack.mallocLong(1);
            for (var i = 0; i < numImages; i++) {
                attachmentsBuff.put(0, swapChain.getImageViews()[i].vk());
                this.frameBuffers[i] = new FrameBuffer(swapChain.getDevice(), width, height, attachmentsBuff, this.lightingRenderPass.vk(), 1);
            }
        }
    }

    public FrameBuffer[] getFrameBuffers() {
        return this.frameBuffers;
    }

    public LightingRenderPass getLightingRenderPass() {
        return this.lightingRenderPass;
    }

    public void resize(Swapchain swapChain) {
        Arrays.stream(this.frameBuffers).forEach(FrameBuffer::close);
        createFrameBuffers(swapChain);
    }
}