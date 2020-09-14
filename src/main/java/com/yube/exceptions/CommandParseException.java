package com.yube.exceptions;

public class CommandParseException extends RuntimeException {

    public CommandParseException() {
    }

    public CommandParseException(String message) {
        super(message);
    }

    public CommandParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandParseException(Throwable cause) {
        super(cause);
    }
}
