package me.hydos.alchemytools.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RootDirectoryLocator implements ModelLocator {

    private final Path root;

    public RootDirectoryLocator(Path root) {
        this.root = root;
    }

    @Override
    public byte[] getFile(String name) {
        try {
            return Files.readAllBytes(root.resolve(name));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + name, e);
        }
    }

    @Override
    public BufferedImage readImage(List<String> layers) {
        return null;
    }

    @Override
    public BufferedImage readImage(String name) {
        return null;
    }
}
