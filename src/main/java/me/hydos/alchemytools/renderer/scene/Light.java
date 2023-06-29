package me.hydos.alchemytools.renderer.scene;

import org.joml.Vector4f;

public class Light {

    private final Vector4f color;
    /**
     * For directional lights, the "w" coordinate will be 0. For point lights it will be "1". For directional lights
     * this attribute should be read as a direction.
     */
    private final Vector4f position;

    public Light(float r, float g, float b, float w) {
        this.color = new Vector4f(r, g, b, w);
        this.position = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public Vector4f getColor() {
        return this.color;
    }

    public Vector4f getPosition() {
        return this.position;
    }
}
