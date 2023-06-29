package me.hydos.alchemytools.renderer.impl;

import me.hydos.alchemytools.renderer.wrapper.image.Texture;
import me.hydos.alchemytools.renderer.wrapper.init.Device;
import me.hydos.alchemytools.util.IndexedLinkedHashMap;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TextureCache implements Closeable {

    private final IndexedLinkedHashMap<String, Texture> textureMap;

    public TextureCache() {
        this.textureMap = new IndexedLinkedHashMap<>();
    }

    @Override
    public void close() {
        this.textureMap.forEach((k, v) -> v.close());
        this.textureMap.clear();
    }

//    public Texture createTexture(Device device, String id, BufferedImage cpuTexture, boolean transparent, int format) {
//        var texture = this.textureMap.get(id);
//        if (texture == null) {
//            texture = new Texture(device, id, cpuTexture, transparent, format);
//            this.textureMap.put(id, texture);
//        }
//        return texture;
//    }

    public void createTexture(Device device, String id, ByteBuffer cpuTexture, int width, int height, boolean transparent, int format) {
        var texture = this.textureMap.get(id);
        if (texture == null) {
            texture = new Texture(device, id, cpuTexture, width, height, transparent, format);
            this.textureMap.put(id, texture);
        }
    }

    public List<Texture> getAll() {
        return new ArrayList<>(this.textureMap.values());
    }

    public int getPosition(String texturePath) {
        var result = -1;
        if (texturePath != null) result = this.textureMap.getIndexOf(texturePath);
        return result;
    }

    public Texture getTexture(String texturePath) {
        return this.textureMap.get(texturePath);
    }
}