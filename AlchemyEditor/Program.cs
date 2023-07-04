using RenderClient.packets.init;

namespace AlchemyEditor;

internal static class Program {

    public static void Main() {
        // var ipEndPoint = new IPEndPoint(IPAddress.Loopback, 25252);
        // using TcpClient client = new();
        // client.ConnectAsync(ipEndPoint);
        // using var stream = client.GetStream();
        //
        // stream.Write(CreateSetupPacket());
        
        
        var client = new RenderClient.RenderClient(25252);
        client.SendPacket(new C2SInitRenderer(1920, 1080, "app name", "a window title from C#"));

        while (true) {
        }
    }
}
