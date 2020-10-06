package com.yube.utils.log;

import java.time.LocalDateTime;

public class LogEntry {

    private LogLevel logLevel;
    private String message;
    private LocalDateTime time;

    public LogEntry() {
    }

    public LogEntry(LogLevel logLevel, String message, LocalDateTime time) {
        this.logLevel = logLevel;
        this.message = message;
        this.time = time;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
