package com.yube.utils;

import com.yube.utils.log.LogLevel;
import com.yube.utils.log.RestLogger;

public final class ExceptionHandler {

    private final static RestLogger logger = RestLogger.getInstance();

    public static void processException(Exception e) {
        logger.log(LogLevel.ERROR, e.getMessage());
    }
}
