package com.yube.utils.log;

import com.google.gson.Gson;
import com.yube.utils.RestClient;

import java.time.LocalDateTime;

public final class RestLogger {

    private static RestLogger instance = new RestLogger();
    private final RestClient restClient = new RestClient();
    private final Gson gson = new Gson();

    private RestLogger() {
    }

    public static RestLogger getInstance() {
        return instance;
    }

    public void log(LogLevel level, String message) {
        LogEntry logEntry = new LogEntry(level, message, LocalDateTime.now());
        String logJson = gson.toJson(logEntry);
        restClient.post("http://127.0.0.1:8080", logJson);
    }
}
