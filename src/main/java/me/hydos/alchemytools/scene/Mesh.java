package me.hydos.alchemytools.scene;

public record Mesh(
        String id,
        float[] positions,
        float[] colors,
        float[] normals,
        float[] uv0,
        float[] uv1,
        float[] uv2,
        int[] indices
) {

    public boolean colorEnabled() {
        return colors.length > 0;
    }

    public boolean normalsEnabled() {
        return normals.length > 0;
    }

    public boolean uv0Enabled() {
        return uv0.length > 0;
    }

    public boolean uv1Enabled() {
        return uv1.length > 0;
    }

    public boolean uv2Enabled() {
        return uv2.length > 0;
    }

    @Override
    public String toString() {
        return "Mesh[i" + id + "]";
    }
}