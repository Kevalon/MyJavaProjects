package com.uni.log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Logger {

    private Path logPath = Path.of("src/main/resources/log.txt");

    public Logger(String logPath) {
        this.logPath = Path.of(logPath);
    }

    public Logger(){}

    public void append(String message) throws IOException {
       Files.writeString(logPath, message, StandardOpenOption.APPEND);
    }
}
