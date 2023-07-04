namespace RenderClient.packets.init; 

public class C2SInitRenderer : Packet {
    private readonly int _width;
    private readonly int _height;
    private readonly string _applicationName;
    private readonly string _windowTitle;

    public C2SInitRenderer(int width, int height, string applicationName, string windowTitle) {
        _width = width;
        _height = height;
        _applicationName = applicationName;
        _windowTitle = windowTitle;
    }

    public void Write(BinaryWriter writer) {
        writer.Write(_width);
        writer.Write(_height);
        writer.WriteUtf8(_applicationName);
        writer.WriteUtf8(_windowTitle);
    }

    public void Handle() {
        throw new NotImplementedException();
    }
}