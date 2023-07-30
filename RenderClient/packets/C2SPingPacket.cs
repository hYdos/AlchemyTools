namespace RenderClient.packets;

public class C2SPingPacket : Packet {
    private static readonly DateTime Jan1St1970 = new(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);

    public void Write(BinaryWriter writer) {
        writer.Write(CurrentTimeMillis());
    }

    public void Handle() {
        throw new NotImplementedException();
    }
    
    private static long CurrentTimeMillis() {
        return (long)(DateTime.UtcNow - Jan1St1970).TotalMilliseconds;
    }
}