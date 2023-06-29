package me.hydos.alchemytools.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Objects;

public final class MeshInstance {
    public final String id;
    public final Vector3f position;
    public final Vector3f rotation;
    public final Vector3f scale;
    private final Matrix4f modelMatrix = new Matrix4f();

    public MeshInstance(String meshId, Vector3f position, Vector3f rotation, Vector3f scale) {
        this.id = meshId;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public MeshInstance(String meshId) {
        this(meshId, new Vector3f(), new Vector3f(), new Vector3f());
    }

    public void recalculate() {
        modelMatrix.identity()
                .rotateXYZ(rotation)
                .translate(position)
                .scale(scale);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MeshInstance) obj;
        return Objects.equals(this.id, that.id) && Objects.equals(this.position, that.position) && Objects.equals(this.rotation, that.rotation) && Objects.equals(this.scale, that.scale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position, rotation, scale);
    }

    @Override
    public String toString() {
        return "MeshInstance[" + "id=" + id + ", " + "position=" + position + ", " + "rotation=" + rotation + ", " + "scale=" + scale + ']';
    }
}
