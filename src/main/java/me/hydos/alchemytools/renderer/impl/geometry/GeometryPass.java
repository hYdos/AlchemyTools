package me.hydos.alchemytools.renderer.impl.geometry;

import me.hydos.alchemytools.renderer.impl.GlobalBuffers;
import me.hydos.alchemytools.renderer.impl.ImplUtils;
import me.hydos.alchemytools.renderer.impl.TextureCache;
import me.hydos.alchemytools.renderer.scene.Scene;
import me.hydos.alchemytools.renderer.wrapper.cmd.CmdBuffer;
import me.hydos.alchemytools.renderer.wrapper.core.*;
import me.hydos.alchemytools.renderer.wrapper.descriptor.DescriptorPool;
import me.hydos.alchemytools.renderer.wrapper.descriptor.DescriptorSet;
import me.hydos.alchemytools.renderer.wrapper.descriptor.DescriptorSetLayout;
import me.hydos.alchemytools.renderer.wrapper.image.TextureDescriptorSet;
import me.hydos.alchemytools.renderer.wrapper.image.TextureSampler;
import me.hydos.alchemytools.renderer.wrapper.init.Device;
import me.hydos.alchemytools.renderer.wrapper.manager.PoolManager;
import me.hydos.alchemytools.renderer.wrapper.memory.MemoryBarrier;
import me.hydos.alchemytools.renderer.wrapper.pipeline.Pipeline;
import me.hydos.alchemytools.renderer.wrapper.pipeline.PipelineCache;
import me.hydos.alchemytools.renderer.wrapper.pipeline.ShaderConstants;
import me.hydos.alchemytools.renderer.wrapper.pipeline.ShaderProgram;
import me.hydos.alchemytools.renderer.wrapper.renderpass.Attachment;
import me.hydos.alchemytools.renderer.wrapper.vertex.InstancedVertexBufferStructure;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkViewport;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.vulkan.VK11.*;

public class GeometryPass implements Closeable {
    private static final String GEOMETRY_FRAGMENT_SHADER_FILE_GLSL = "geometry_fragment.glsl";
    private static final String GEOMETRY_FRAGMENT_SHADER_FILE_SPV = GEOMETRY_FRAGMENT_SHADER_FILE_GLSL + ".spv";
    private static final String GEOMETRY_VERTEX_SHADER_FILE_GLSL = "geometry_vertex.glsl";
    private static final String GEOMETRY_VERTEX_SHADER_FILE_SPV = GEOMETRY_VERTEX_SHADER_FILE_GLSL + ".spv";

    private final Device device;
    private final GeometryFrameBuffer frameBuffer;
    private final ShaderConstants constants;
    private final MemoryBarrier barrier;
    private final PipelineCache cache;
    private final Scene scene;

    private PoolManager pools;
    private DescriptorSetLayout[] descSetLayouts;
    private DescriptorSetLayout.DynUniformDescriptorSetLayout materialDescSetLayout;
    private DescriptorSet.StorageDescriptorSet materialDescSet;
    private Pipeline pipeline;
    private DescriptorSet.UniformDescriptorSet projMatrixDescriptorSet;
    private VkBuffer projMatrixUniform;
    private ShaderProgram shaderProgram;
    private DescriptorSetLayout.StorageDescriptorSetLayout storageDescriptorSetLayout;
    private Swapchain swapChain;
    private TextureDescriptorSet textureDescriptorSet;
    private DescriptorSetLayout.SamplerDescriptorSetLayout textureDescriptorSetLayout;
    private TextureSampler textureSampler;
    private DescriptorSetLayout.UniformDescriptorSetLayout uniformDescriptorSetLayout;
    private VkBuffer[] viewMatricesBuffer;
    private DescriptorSet.UniformDescriptorSet[] viewMatricesDescriptorSets;

    public GeometryPass(Swapchain swapChain, PipelineCache cache, Scene scene, GlobalBuffers globalBuffers) {
        this.swapChain = swapChain;
        this.cache = cache;
        this.scene = scene;
        this.device = swapChain.getDevice();
        this.constants = new ShaderConstants.Builder()
                .entry(Integer.BYTES, data -> data.putInt(Configuration.getInstance().getMaxTextures()))
                .build();

        this.frameBuffer = new GeometryFrameBuffer(swapChain);
        var numImages = swapChain.getImageCount();
        createShaders();
        createDescriptorPool();
        createDescriptorSets(numImages, globalBuffers);
        System.out.println("please un-hardcode createPipeline in " + this.getClass().getName() + " at some point ");
        createPipeline(3); // FIXME: NO
        VkUtils.copyMatrixToBuffer(this.projMatrixUniform, scene.getProjection().getProjectionMatrix());
        this.barrier = new MemoryBarrier(VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
    }

    public void close() {
        this.pipeline.close();
        this.constants.close();
        Arrays.stream(this.viewMatricesBuffer).forEach(VkBuffer::close);
        this.projMatrixUniform.close();
        this.textureSampler.close();
        this.materialDescSetLayout.close();
        this.textureDescriptorSetLayout.close();
        this.uniformDescriptorSetLayout.close();
        this.storageDescriptorSetLayout.close();
        this.pools.close();
        this.shaderProgram.close();
        this.frameBuffer.close();
    }

    private void createDescriptorPool() {
        var settings = Configuration.getInstance();
        var descriptorTypeCounts = new ArrayList<DescriptorPool.DescriptorTypeCount>();
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(this.swapChain.getImageCount() + 1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(settings.getMaxMaterials() * 3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC));
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(1, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER));
        this.pools = new PoolManager(this.device, descriptorTypeCounts);
    }

    private void createDescriptorSets(int numImages, GlobalBuffers globalBuffers) {
        var settings = Configuration.getInstance();
        this.uniformDescriptorSetLayout = new DescriptorSetLayout.UniformDescriptorSetLayout(this.device, 0, VK_SHADER_STAGE_VERTEX_BIT);
        this.textureDescriptorSetLayout = new DescriptorSetLayout.SamplerDescriptorSetLayout(this.device, settings.getMaxTextures(), 0, VK_SHADER_STAGE_FRAGMENT_BIT);
        this.materialDescSetLayout = new DescriptorSetLayout.DynUniformDescriptorSetLayout(this.device, 0, VK_SHADER_STAGE_FRAGMENT_BIT);
        this.storageDescriptorSetLayout = new DescriptorSetLayout.StorageDescriptorSetLayout(this.device, 0, VK_SHADER_STAGE_FRAGMENT_BIT);
        this.descSetLayouts = new DescriptorSetLayout[]{
                this.uniformDescriptorSetLayout,
                this.uniformDescriptorSetLayout,
                this.storageDescriptorSetLayout,
                this.textureDescriptorSetLayout
        };

        this.textureSampler = new TextureSampler(this.device, 1);
        this.projMatrixUniform = new VkBuffer(this.device, VkConstants.MAT4X4_SIZE, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
        this.projMatrixDescriptorSet = new DescriptorSet.UniformDescriptorSet(this.pools.getPool(), this.uniformDescriptorSetLayout, this.projMatrixUniform, 0);
        this.materialDescSet = new DescriptorSet.StorageDescriptorSet(this.pools.getPool(), this.storageDescriptorSetLayout, globalBuffers.getMaterialsBuffer(), 0);

        this.viewMatricesDescriptorSets = new DescriptorSet.UniformDescriptorSet[numImages];
        this.viewMatricesBuffer = new VkBuffer[numImages];
        for (var i = 0; i < numImages; i++) {
            this.viewMatricesBuffer[i] = new VkBuffer(this.device, VkConstants.MAT4X4_SIZE, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
            this.viewMatricesDescriptorSets[i] = new DescriptorSet.UniformDescriptorSet(this.pools.getPool(), this.uniformDescriptorSetLayout, this.viewMatricesBuffer[i], 0);
        }
    }

    private void createPipeline(int colorAttachmentCount) {
        var creationInfo = new Pipeline.PipeLineCreationInfo(this.frameBuffer.getRenderPass().vk(), this.shaderProgram, colorAttachmentCount, true, true, 0, new InstancedVertexBufferStructure(), descSetLayouts);
        this.pipeline = new Pipeline(this.cache, creationInfo);
        creationInfo.close();
    }

    private void createShaders() {
        this.shaderProgram = new ShaderProgram(this.device, new ShaderProgram.ShaderData[]{
                new ShaderProgram.ShaderData(VK_SHADER_STAGE_VERTEX_BIT, ImplUtils.get(GEOMETRY_VERTEX_SHADER_FILE_SPV)),
                new ShaderProgram.ShaderData(VK_SHADER_STAGE_FRAGMENT_BIT, ImplUtils.get(GEOMETRY_FRAGMENT_SHADER_FILE_SPV), constants)
        });
    }

    public List<Attachment> getAttachments() {
        return this.frameBuffer.getRenderPass().attachments;
    }

    public void loadModels(TextureCache textureCache) {
        this.device.waitIdle();
        // Size of the descriptor is set up in the layout, we need to fill up the texture list
        // up to the number defined in the layout (reusing last texture)
        var textureCacheList = textureCache.getAll();
        var textureCacheSize = textureCacheList.size();
        var textureList = new ArrayList<>(textureCacheList);
        var settings = Configuration.getInstance();
        var maxTextures = settings.getMaxTextures();

        if (!textureList.isEmpty()) {
            for (var i = 0; i < maxTextures - textureCacheSize; i++)
                textureList.add(textureCacheList.get(textureCacheSize - 1));
            this.textureDescriptorSet = new TextureDescriptorSet(this.pools.getPool(), this.textureDescriptorSetLayout, textureList, this.textureSampler, 0);
        }
    }

    public void recordCommandBuffer(CmdBuffer cmdBuffer, GlobalBuffers globalBuffers, int idx) {
        try (var stack = MemoryStack.stackPush()) {
            var swapChainExtent = this.swapChain.getSwapChainExtent();
            var width = swapChainExtent.width();
            var height = swapChainExtent.height();

            var frameBuffer = this.frameBuffer.getFrameBuffer();
            var attachments = this.frameBuffer.getRenderPass().attachments;
            var clearValues = VkClearValue.calloc(attachments.size(), stack);
            for (var attachment : attachments)
                if (attachment.isDepthAttachment()) clearValues.apply(v -> v.depthStencil().depth(1.0f));
                else
                    clearValues.apply(v -> v.color().float32(0, 0.0f).float32(1, 0.0f).float32(2, 0.0f).float32(3, 1));
            clearValues.flip();

            var renderPassBeginInfo = VkRenderPassBeginInfo.calloc(stack).sType$Default().renderPass(this.frameBuffer.getRenderPass().vk()).pClearValues(clearValues).renderArea(a -> a.extent().set(width, height)).framebuffer(frameBuffer.vk());
            var cmdHandle = cmdBuffer.vk();

            vkCmdPipelineBarrier(cmdHandle, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_VERTEX_INPUT_BIT, 0, this.barrier.vk(), null, null);
            vkCmdBeginRenderPass(cmdHandle, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);
            vkCmdBindPipeline(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, this.pipeline.vk());

            var viewport = VkViewport.calloc(1, stack)
                    .x(0)
                    .y(height)
                    .height(-height)
                    .width(width)
                    .minDepth(0.0f)
                    .maxDepth(1.0f);
            vkCmdSetViewport(cmdHandle, 0, viewport);

            var scissor = VkRect2D.calloc(1, stack).extent(it -> it
                    .width(width)
                    .height(height)
            ).offset(it -> it
                    .x(0)
                    .y(0)
            );
            vkCmdSetScissor(cmdHandle, 0, scissor);

            var descriptorSets = stack.mallocLong(textureDescriptorSet != null ? 4 : 3)
                    .put(0, this.projMatrixDescriptorSet.vk())
                    .put(1, this.viewMatricesDescriptorSets[idx].vk())
                    .put(2, this.materialDescSet.vk());

            if(textureDescriptorSet != null) descriptorSets.put(3, this.textureDescriptorSet.vk());

            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, this.pipeline.layout, 0, descriptorSets, null);

            var vertexBuffer = stack.mallocLong(1);
            var instanceBuffer = stack.mallocLong(1);
            var offsets = stack.mallocLong(1).put(0, 0L);

            // Draw commands for non-animated entities
            if (globalBuffers.getNumIndirectCommands() > 0) {
                vertexBuffer.put(0, globalBuffers.getVerticesBuffer().getBuffer());
                instanceBuffer.put(0, globalBuffers.getInstanceDataBuffers()[idx].getBuffer());

                vkCmdBindVertexBuffers(cmdHandle, 0, vertexBuffer, offsets);
                vkCmdBindVertexBuffers(cmdHandle, 1, instanceBuffer, offsets);
                vkCmdBindIndexBuffer(cmdHandle, globalBuffers.getIndicesBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
                var staticIndirectBuffer = globalBuffers.getIndirectBuffer();
                vkCmdDrawIndexedIndirect(cmdHandle, staticIndirectBuffer.getBuffer(), 0, globalBuffers.getNumIndirectCommands(), GlobalBuffers.IND_COMMAND_STRIDE);
            }

            // Draw commands for animated entities
            if (globalBuffers.getNumAnimIndirectCommands() > 0) {
                vertexBuffer.put(0, globalBuffers.getAnimVerticesBuffer().getBuffer());
                instanceBuffer.put(0, globalBuffers.getAnimInstanceDataBuffers()[idx].getBuffer());

                vkCmdBindVertexBuffers(cmdHandle, 0, vertexBuffer, offsets);
                vkCmdBindVertexBuffers(cmdHandle, 1, instanceBuffer, offsets);
                vkCmdBindIndexBuffer(cmdHandle, globalBuffers.getIndicesBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
                var animIndirectBuffer = globalBuffers.getAnimIndirectBuffer();
                vkCmdDrawIndexedIndirect(cmdHandle, animIndirectBuffer.getBuffer(), 0, globalBuffers.getNumAnimIndirectCommands(), GlobalBuffers.IND_COMMAND_STRIDE);
            }

            vkCmdEndRenderPass(cmdHandle);
        }
    }

    public void render() {
        var idx = this.swapChain.getCurrentFrame();
        VkUtils.copyMatrixToBuffer(this.projMatrixUniform, this.scene.getProjection().getProjectionMatrix());
        VkUtils.copyMatrixToBuffer(this.viewMatricesBuffer[idx], this.scene.getCamera().viewMatrix);
    }

    public void resize(Swapchain swapChain) {
        this.swapChain = swapChain;
        this.frameBuffer.resize(swapChain);
    }
}