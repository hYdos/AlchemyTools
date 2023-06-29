package me.hydos.alchemytools;

import me.hydos.alchemytools.io.Display;
import me.hydos.alchemytools.io.Window;
import me.hydos.alchemytools.renderer.impl.Renderer;
import me.hydos.alchemytools.renderer.scene.Light;
import me.hydos.alchemytools.renderer.scene.ModelData;
import me.hydos.alchemytools.renderer.scene.ModelProcessor;
import me.hydos.alchemytools.renderer.scene.RenderEntity;
import me.hydos.alchemytools.renderer.wrapper.init.VulkanCreationContext;
import me.hydos.alchemytools.util.RootDirectoryLocator;
import me.hydos.alchemytools.util.assimp.AssimpModelLoader;
import me.hydos.alchemytools.util.model.SceneNode;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;

public class AssimpTestScene {
    public final Renderer renderer;
    public final me.hydos.alchemytools.renderer.scene.Scene scene;
    private final List<ModelData> models = new ArrayList<>();
    public final Display display;

    public AssimpTestScene() {
        System.loadLibrary("renderdoc");
        this.display = new Window("Assimp Test Scene");
        this.scene = new me.hydos.alchemytools.renderer.scene.Scene(display);
        this.renderer = new Renderer(display, new VulkanCreationContext("assimp_test_scene"), this.scene);
        onInitialize();

        while (display.keepWindowOpen()) {
            display.pollEvents();
            renderer.render(display, this.scene);
        }

        renderer.close();
        display.close();
    }

    private void onInitialize() {
        var locator = new RootDirectoryLocator(Paths.get("C:/Users/hydos/Desktop"));
        var assimpModels = AssimpModelLoader.load(
                "nowords.glb",
                locator,
                aiProcess_GenSmoothNormals
                | aiProcess_EmbedTextures
                | aiProcess_FixInfacingNormals
                | aiProcess_CalcTangentSpace
                | aiProcess_LimitBoneWeights
        );

        loadTextures(assimpModels[0]);

        for (var model : assimpModels) {
            var data = ModelProcessor.loadModel(model.name(), locator, model, List.of());

            var e = new RenderEntity("some entity_" + model.name(), model); // TODO: multiple models for 1 entity? maybe even a tree scene layout

            scene.addEntity(e);
            models.add(data);
        }

        renderer.loadModels(models);

        var camera = scene.getCamera();
        camera.setPosition(6, 2, -2);
        camera.setRotation((float) 0, (float) Math.toRadians(-100));
        camera.recalculate();

        scene.setLights(new Light[]{
                new Light(0.2f, 0.2f, 0.2f, 1.0f)
        });
    }

    private void loadTextures(SceneNode sceneNode) {
        for (int i = 0; i < sceneNode.textures().length; i++) {
            var texture = sceneNode.textures()[i];
            renderer.textureCache.createTexture(renderer.device, "*" + i, texture.data(), texture.width(), texture.height(), false, VK_FORMAT_R8G8B8A8_SRGB);
        }
    }

    public static void main(String[] args) {
        new AssimpTestScene();
    }
}
