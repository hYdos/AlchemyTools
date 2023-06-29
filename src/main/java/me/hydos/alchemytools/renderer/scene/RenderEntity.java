package me.hydos.alchemytools.renderer.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RenderEntity {

    private final String id;
    private final String modelId;
    public final Matrix4f modelMatrix;
    private final Vector3f position;
    private final Quaternionf rotation;
    private AnimationInstance animationInstance;
    private float scale;

    public RenderEntity(String id, String modelId, Vector3f position) {
        this.id = id;
        this.modelId = modelId;
        this.position = position;
        this.scale = 1;
        this.rotation = new Quaternionf();
        this.modelMatrix = new Matrix4f();
        recalculate();
    }

    public AnimationInstance getAnimation() {
        return this.animationInstance;
    }

    public void setEntityAnimation(AnimationInstance instance) {
        this.animationInstance = instance;
    }

    public String getId() {
        return this.id;
    }

    public String getModelId() {
        return this.modelId;
    }

    public Matrix4f getModelMatrix() {
        return this.modelMatrix;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public Quaternionf getRotation() {
        return this.rotation;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        recalculate();
    }

    public boolean hasAnimation() {
        return this.animationInstance != null;
    }

    public final void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
        recalculate();
    }

    public void recalculate() {
        this.modelMatrix.translationRotateScale(this.position, this.rotation, this.scale);
    }

    public static class AnimationInstance {
        public int animationIdx;
        public int currentFrame;
        public boolean playing;

        public AnimationInstance(boolean playing, int animationIdx, int currentFrame) {
            this.playing = playing;
            this.animationIdx = animationIdx;
            this.currentFrame = currentFrame;
        }
    }
}