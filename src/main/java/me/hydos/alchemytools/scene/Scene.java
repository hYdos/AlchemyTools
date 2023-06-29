package me.hydos.alchemytools.scene;

import me.hydos.alchemytools.renderer.scene.Light;

import java.util.List;

/**
 * Abstracted representation of a scene. Allows different sources of a scene for example an Assimp Scene or an Alchemy Scene
 */
public interface Scene {

    List<MeshInstance> getStaticObjects();

    List<DynamicObject> getDynamicObjects();

    List<DynamicObject> getAnimatedObjects();

    List<Light> getLights();

    Light getDirectionalLight();
}
