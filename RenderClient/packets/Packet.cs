namespace RenderClient; 

public interface Packet {
    
    /**
     * Called inside the network thread and should be quick conversion of the data to bytes for sending to the server
     */
    void Write(BinaryWriter writer);

    /**
     * Called outside the Network Thread for handling incoming packets from the server
     */
    void Handle();
}