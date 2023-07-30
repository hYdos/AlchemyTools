using RenderClient.packets;

namespace AlchemyEditor;

internal static class Program {

    public static async Task Main() {
        var client = new RenderClient.RenderClient(25252);
        client.SendPacket(new C2SInitRenderer(1920, 1080, "app name", "a window title from C#"));

        var clientTask = client.Start();
        var gameTask = GameLogic();
    
        await Task.WhenAll(clientTask, gameTask);
    
        Console.WriteLine("ok");
    }
    
    private static async Task GameLogic() {
        while (true) {
            // Perform game/app logic here
            await Task.Delay(100); // Adjust the delay time as needed
        }
    }
}