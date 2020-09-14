package com.yube.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Validator {

    public static boolean validateDateTime(String pattern, String value) {
        try {
            LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
