package me.hydos.alchemytools;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class RenderServer implements Closeable {

    private final ServerSocket renderServer;
    private final Socket socket;

    public RenderServer(int port) throws IOException {
        this.renderServer = new ServerSocket(port);
        this.socket = renderServer.accept();
        var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        var greeting = reader.readLine();
        if ("hello server".equals(greeting)) System.out.println("hello client");
        else System.out.println("unrecognised greeting");
    }

    @Override
    public void close() throws IOException {
        socket.close();
        renderServer.close();
    }

    public static void main(String[] args) throws IOException {
        var port = 25252;
        if(args.length > 0) port = Integer.parseInt(args[0]);
        new RenderServer(port);
    }
}
