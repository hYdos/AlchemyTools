package me.hydos.alchemytools.network;

import me.hydos.alchemytools.io.Window;
import me.hydos.alchemytools.renderer.impl.Renderer;
import me.hydos.alchemytools.renderer.scene.Scene;
import me.hydos.alchemytools.util.LoggerSetup;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class RenderThread {
    private static RenderThread instance;
    private final Collection<Consumer<RenderThread>> actionQueue = Collections.synchronizedCollection(new ArrayList<>());
    public Window window;
    public Scene scene;
    public Renderer renderer;

    public RenderThread(int networkPort) {
        var server = new RenderServer(networkPort);
        instance = this;
        server.start();

        while (!server.shouldEnd()) {
            var currentQueue = new ArrayList<>(actionQueue);
            for (var runnable : currentQueue) runnable.accept(this);
            actionQueue.removeAll(currentQueue);
            currentQueue.clear();

            if(window != null) window.pollEvents();
        }

        GLFW.glfwHideWindow(window.handle());
        if (renderer != null) renderer.close();
        if (window != null) window.close();
    }

    public static void queue(Consumer<RenderThread> action) {
        if (getInstance() == null) throw new RuntimeException("Queued too early!");
        getInstance().actionQueue.add(action);
    }

    public static RenderThread getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        LoggerSetup.onInitialize();
        var port = 25252;
        if (args.length > 0) port = Integer.parseInt(args[0]);

        new RenderThread(port);
    }
}
