package me.hydos.alchemytools.renderer.scene;

import me.hydos.alchemytools.io.Display;
import me.hydos.alchemytools.renderer.wrapper.core.VkConstants;
import org.joml.Vector4f;

import java.util.*;

public class Scene {

    private final Vector4f ambientLight;
    private final Camera camera;
    private final Map<String, List<RenderEntity>> entitiesMap;
    public final Projection projection;
    private Light directionalLight;
    private long entitiesLoadedTimeStamp;
    private boolean lightChanged;
    private Light[] lights;

    public Scene(Display display) {
        this.entitiesMap = new HashMap<>();
        this.projection = new Projection();
        this.projection.resize(display.getWidth(), display.getHeight());
        this.camera = new Camera();
        this.ambientLight = new Vector4f();
    }

    public void addEntity(RenderEntity entity) {
        var entities = this.entitiesMap.computeIfAbsent(entity.getModelId(), k -> new ArrayList<>());
        entities.add(entity);
        this.entitiesLoadedTimeStamp = System.currentTimeMillis();
    }

    public Vector4f getAmbientLight() {
        return this.ambientLight;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public Light getDirectionalLight() {
        return this.directionalLight;
    }

    public List<RenderEntity> getEntitiesByModelId(String modelId) {
        return this.entitiesMap.get(modelId);
    }

    public long lastEntityLoadTime() {
        return this.entitiesLoadedTimeStamp;
    }

    public Map<String, List<RenderEntity>> getEntitiesMap() {
        return this.entitiesMap;
    }

    public Light[] getLights() {
        return this.lights;
    }

    public void setLights(Light[] lights) {
        this.directionalLight = null;
        var numLights = lights != null ? lights.length : 0;
        if (numLights > VkConstants.MAX_LIGHTS)
            throw new RuntimeException("Maximum number of lights set to: " + VkConstants.MAX_LIGHTS);
        this.lights = lights;
        var option = Arrays.stream(lights).filter(l -> l.getPosition().w == 0).findFirst();
        option.ifPresent(light -> this.directionalLight = light);

        this.lightChanged = true;
    }

    public Projection getProjection() {
        return this.projection;
    }

    public boolean isLightChanged() {
        return this.lightChanged;
    }

    public void setLightChanged(boolean lightChanged) {
        this.lightChanged = lightChanged;
    }

    public void removeAllEntities() {
        this.entitiesMap.clear();
        this.entitiesLoadedTimeStamp = System.currentTimeMillis();
    }

    public void removeEntity(RenderEntity entity) {
        var entities = this.entitiesMap.get(entity.getModelId());
        if (entities != null) entities.removeIf(e -> e.getId().equals(entity.getId()));
        this.entitiesLoadedTimeStamp = System.currentTimeMillis();
    }
}
