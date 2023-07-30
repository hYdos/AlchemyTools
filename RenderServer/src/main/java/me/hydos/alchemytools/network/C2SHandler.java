package me.hydos.alchemytools.network;

import me.hydos.alchemytools.io.Window;
import me.hydos.alchemytools.network.packets.C2SInitRenderer;
import me.hydos.alchemytools.renderer.impl.Renderer;
import me.hydos.alchemytools.renderer.scene.Scene;
import me.hydos.alchemytools.renderer.wrapper.init.VulkanCreationContext;

public class C2SHandler {

    public static void onInitRenderer(C2SInitRenderer packet, RenderThread ctx) {
        ctx.window = new Window(packet.title, packet.width, packet.height);
        ctx.scene = new Scene(ctx.window);
        ctx.renderer = new Renderer(ctx.window, new VulkanCreationContext(packet.appName), ctx.scene);
    }
}
