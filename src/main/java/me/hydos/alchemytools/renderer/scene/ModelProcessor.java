package me.hydos.alchemytools.renderer.scene;

import me.hydos.alchemytools.renderer.wrapper.core.Configuration;
import me.hydos.alchemytools.util.ModelLocator;
import me.hydos.alchemytools.util.Pair;
import me.hydos.alchemytools.util.model.Mesh;
import me.hydos.alchemytools.util.model.Model;
import me.hydos.alchemytools.util.model.animation.Animation;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static me.hydos.alchemytools.renderer.wrapper.core.DataUtils.toPrimitiveFloatArr;
import static me.hydos.alchemytools.renderer.wrapper.core.DataUtils.toPrimitiveIntArr;

/**
 * Handles processing a model loaded from rks modelLoader into a format the renderer understands
 */
public class ModelProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelProcessor.class);
    public static final int MAX_JOINTS = 200;
    public static final int MAX_WEIGHTS = 4;

    public static ModelData loadModel(String modelId, ModelLocator locator, Model model, List<Animation> animations) {
        LOGGER.info("Loading model \"{}\"", modelId);

        var meshDataList = new ArrayList<ModelData.MeshData>();
        for (var i = 0; i < model.meshes().length; i++) {
            var meshData = processMesh(model.meshes()[i]);
            meshDataList.add(meshData);
        }

        var modelData = new ModelData(modelId, meshDataList, model.materials(), locator);

        if (animations.size() > 0) {
            LOGGER.info("Processing animations");
            List<ModelData.AnimMeshData> animMeshDataList = new ArrayList<>();
            for (var i = 0; i < model.meshes().length; i++) {
                var animMeshData = processBones(model.meshes()[i], model);
                animMeshDataList.add(animMeshData);
            }

            modelData.setAnimMeshDataList(animMeshDataList);
            modelData.setAnimations(processAnimations(animations));
        }

        LOGGER.info("Loaded model [{}]", modelId);
        return modelData;
    }

    private static List<ModelData.PreComputedAnimation> processAnimations(List<Animation> animations) {
        var processedAnimations = new ArrayList<ModelData.PreComputedAnimation>();
        var ups = Configuration.getInstance().getUpdatesPerSecond();
        for (var animation : animations) {
            var framesNeeded = (ups / animation.ticksPerSecond) * animation.animationDuration;
            var frames = new ArrayList<Matrix4f[]>();
            for (var i = 0; i < framesNeeded; i++) frames.add(animation.getFrameTransform(i));
            processedAnimations.add(new ModelData.PreComputedAnimation(animation.name, animation.animationDuration, frames));
        }

        return processedAnimations;
    }

    private static ModelData.AnimMeshData processBones(Mesh mesh, Model model) {
        var vertBoneWeights = new HashMap<Integer, List<Pair<Integer, Float>>>();
        var boneIds = new ArrayList<Integer>();
        var weights = new ArrayList<Float>();

        for (var bone : mesh.bones())
            for (var i = 0; i < bone.weights.length; i++) {
                var weight = bone.weights[i];
                var map = vertBoneWeights.computeIfAbsent(weight.vertexId, integer -> new ArrayList<>());
                map.add(new Pair<>(model.skeleton().getId(bone), weight.weight));
            }

        var vertexCount = mesh.positions().size();
        for (var i = 0; i < vertexCount; i++) {
            var vertexWeights = vertBoneWeights.get(i);
            var size = vertexWeights != null ? vertexWeights.size() : 0;

            for (var j = 0; j < MAX_WEIGHTS; j++)
                if (j < size) {
                    var vertWeight = vertexWeights.get(j);
                    boneIds.add(vertWeight.a());
                    weights.add(vertWeight.b());
                } else {
                    boneIds.add(0);
                    weights.add(0.0f);
                }
        }

        return new ModelData.AnimMeshData(toPrimitiveFloatArr(weights), toPrimitiveIntArr(boneIds));
    }

    private static ModelData.MeshData processMesh(Mesh mesh) {
        var vertices = processVertices(mesh);
        var normals = processNormals(mesh);
        var tangents = processTangents(mesh, normals);
        var biTangents = processBiTangents(mesh, normals);
        var textCoords = processUvs(mesh);
        var indices = mesh.indices();

        // Texture coordinates may not have been populated. We need at least the empty slots
        if (textCoords.isEmpty()) {
            var numElements = (vertices.size() / 3) * 2;
            for (var i = 0; i < numElements; i++) textCoords.add(0.0f);
        }

        return new ModelData.MeshData(
                toPrimitiveFloatArr(vertices),
                toPrimitiveFloatArr(normals),
                toPrimitiveFloatArr(tangents),
                toPrimitiveFloatArr(biTangents),
                toPrimitiveFloatArr(textCoords),
                toPrimitiveIntArr(indices),
                mesh.material()
        );
    }

    private static List<Float> processNormals(Mesh mesh) {
        return mesh.normals().stream()
                .flatMap(vector3f -> Stream.of(vector3f.x, vector3f.y, vector3f.z))
                .toList();
    }

    private static List<Float> processTangents(Mesh mesh, List<Float> normals) {
        var tangents = mesh.tangents().stream()
                .flatMap(vector3f -> Stream.of(vector3f.x, vector3f.y, vector3f.z))
                .toList();

        // Assimp may not calculate tangents with models that do not have texture coordinates. Just create empty values
        if (tangents.isEmpty()) tangents = new ArrayList<>(Collections.nCopies(normals.size(), 0.0f));
        return tangents;
    }

    private static List<Float> processBiTangents(Mesh mesh, List<Float> normals) {
        var biTangents = mesh.biTangents().stream()
                .flatMap(vector3f -> Stream.of(vector3f.x, vector3f.y, vector3f.z))
                .toList();

        // Assimp may not calculate tangents with models that do not have texture coordinates. Just create empty values
        if (biTangents.isEmpty()) biTangents = new ArrayList<>(Collections.nCopies(normals.size(), 0.0f));
        return biTangents;
    }

    private static List<Float> processUvs(Mesh mesh) {
        return mesh.uvs().stream()
                .flatMap(vector3f -> Stream.of(vector3f.x, vector3f.y))
                .toList();
    }

    private static List<Float> processVertices(Mesh mesh) {
        return mesh.positions().stream()
                .flatMap(vector3f -> Stream.of(vector3f.x, vector3f.y, vector3f.z))
                .toList();
    }
}
