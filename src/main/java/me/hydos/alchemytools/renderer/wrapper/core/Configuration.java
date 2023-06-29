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
    private int maxAnimWeightsBuffer = 1000000;
    private int maxIndicesBuffer = 5000000;
    private int maxJointMatricesBuffer = 20000000;
    private int maxMaterials = 500;
    private int maxTextures = maxMaterials * 3;
    private int maxVerticesBuffer = 20000000;
    private int requestedImages = 3;
    private boolean shaderRecompilation = true;
    private float shadowBias = 0.001f;
    private boolean shadowDebug = false;
    private int shadowMapSize = 2048;
    private int ups = 60;
    private boolean vSync = false;
    private boolean validate = true;

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

    public int getMaxAnimWeightsBuffer() {
        return this.maxAnimWeightsBuffer;
    }

    public int getMaxIndicesBuffer() {
        return this.maxIndicesBuffer;
    }

    public int getMaxJointMatricesBuffer() {
        return this.maxJointMatricesBuffer;
    }

    public int getMaxMaterials() {
        return this.maxMaterials;
    }

    public int getMaxTextures() {
        return this.maxTextures;
    }

    public int getMaxVerticesBuffer() {
        return this.maxVerticesBuffer;
    }

    public int getRequestedImages() {
        return this.requestedImages;
    }

    public float getShadowBias() {
        return this.shadowBias;
    }

    public int getShadowMapSize() {
        return this.shadowMapSize;
    }

    public int getUpdatesPerSecond() {
        return this.ups;
    }

    public boolean isShaderRecompilation() {
        return this.shaderRecompilation;
    }

    public boolean isShadowDebug() {
        return this.shadowDebug;
    }

    public boolean isValidate() {
        return this.validate;
    }

    public boolean isvSync() {
        return this.vSync;
    }
}
