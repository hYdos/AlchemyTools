package me.hydos.alchemytools.scene;

import java.util.ArrayList;
import java.util.List;

public class SceneNode {
    public final String name;
    public final List<SceneNode> children;

    public SceneNode(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[name=" + name + "]";
    }
}
