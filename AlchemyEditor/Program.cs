using RenderClient.packets.init;

namespace AlchemyEditor;

internal static class Program {

    public static void Main() {
        var client = new RenderClient.RenderClient(25252);
        client.SendPacket(new C2SInitRenderer(1920, 1080, "app name", "a window title from C#"));

        while (true) {
            // game/app logic would come here so this kinda fakes it for now
        }
    }
}
