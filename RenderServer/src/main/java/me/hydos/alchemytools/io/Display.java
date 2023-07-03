package me.hydos.alchemytools.io;

import java.io.Closeable;

public interface Display extends Closeable {

    @Override
    void close();

    boolean keepWindowOpen();

    void pollEvents();

    boolean isKeyPressed(int glfwKeyW);

    int getWidth();

    int getHeight();

    long handle();

    void setResized(boolean resized);

    boolean isResized();

    void resetResized();
}
