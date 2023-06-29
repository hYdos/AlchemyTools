package me.hydos.alchemytools.renderer.scene;

import org.joml.Matrix4f;

public class Projection {

    public final Matrix4f projectionMatrix;

    public Projection() {
        this.projectionMatrix = new Matrix4f();
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public void resize(int width, int height) {
         this.projectionMatrix.identity();
         this.projectionMatrix.perspective(90, (float) width / (float) height, 0.1f, 10000, true);
    }
}
