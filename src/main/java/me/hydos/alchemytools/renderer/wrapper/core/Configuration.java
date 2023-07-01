package me.hydos.alchemytools.renderer.wrapper.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class Configuration {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = Paths.get("engine.config.json");
    private static final Configuration INSTANCE = Configuration.read();

    // Configuration Settings
    public int animWeightsBufferMaxSize = 1000000;
    public int indexBufferMaxSize = 5000000;
    public int jointMatrixBufferMaxSize = 20000000;
    public int maxMaterials = 500;
    public int maxTextures = maxMaterials * 3;
    public int vertexBufferMaxSize = 20000000;
    public int swapchainImgCount = 3;
    public float shadowBias = 0.001f;
    public boolean shadowDebug = false;
    public int shadowMapSize = 2048;
    public int animationFps = 60; // FIXME: per animation fps should be used
    public boolean enableVsync = false;
    public boolean debug = true;

    /**
     * Internal Use Only. DO NOT CALL
     */
    @ApiStatus.Internal
    public static Configuration read() {
        try {
            if (Files.notExists(CONFIG_FILE)) {
                var config = new Configuration();
                Files.writeString(CONFIG_FILE, GSON.toJson(config));
                return config;
            }

            return GSON.fromJson(Files.readString(CONFIG_FILE), Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + CONFIG_FILE.getFileName(), e);
        }
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }
}
