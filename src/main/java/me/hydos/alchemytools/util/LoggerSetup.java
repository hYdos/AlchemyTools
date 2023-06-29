package me.hydos.alchemytools.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoggerSetup {

    public static void onInitialize() {
        try {
            var logFile = Paths.get("log4j2.alchemytools.xml");
            Files.write(logFile, LoggerSetup.class.getResourceAsStream("/log4j2.alchemytools.xml").readAllBytes());
            System.setProperty("log4j2.configurationFile", logFile.toUri().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
