package com.yube.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.StringJoiner;

@Data
public class ScheduleItem implements Serializable {
    private long chatId;
    private String text;
    private LocalDateTime time;
    private Integer repeatCount;
    private Integer interval;

    @Override
    public String toString() {
        StringJoiner stringJoiner =  new StringJoiner(",\n", ScheduleItem.class.getSimpleName() + " {\n", "\n}\n")
                .add("text = '" + text + "'")
                .add("time = '" + time + "'");
        if (repeatCount != null) stringJoiner.add("repeatCount = '" + repeatCount + "'");
        if (interval != null) stringJoiner.add("interval = '" + interval + "'");
        return stringJoiner.toString();
    }
}
