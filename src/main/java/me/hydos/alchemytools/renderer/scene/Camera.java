package me.hydos.alchemytools.renderer.scene;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;
    public final Vector3f position;
    public final Vector2f rotation;
    public int fov;

    public Camera() {
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.position = new Vector3f();
        this.rotation = new Vector2f();
    }

    public void addRotation(float x, float y) {
        rotation.add(x, y);
        recalculate();
    }

    public void recalculate() {
        viewMatrix.identity()
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .translate(-position.x, -position.y, -position.z);

    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        recalculate();
    }

    public void setRotation(float x, float y) {
        rotation.set(x, y);
        recalculate();
    }
}
