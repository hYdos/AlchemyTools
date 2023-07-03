package me.hydos.alchemytools.scene.gfx;

import me.hydos.alchemytools.scene.SceneNode;
import me.hydos.alchemytools.util.model.Mesh;

import java.util.List;

/**
 * Holds a mesh and combines it with material, and transform data
 */
public class Model extends SceneNode {

    public List<Mesh> meshes;

    public Model(String name) {
        super(name);
    }
}
