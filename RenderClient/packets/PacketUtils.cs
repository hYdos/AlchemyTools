using System.Text;

namespace RenderClient; 

public static class PacketUtils {

    public static void WriteUtf8(this BinaryWriter writer, string str) {
        var stringBytes = Encoding.UTF8.GetBytes(str).ToArray();
        writer.Write(stringBytes.Length);
        writer.Write(stringBytes);
    }
}