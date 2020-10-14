package com.yube.bot;

import com.yube.exceptions.CommandParseException;
import com.yube.model.ScheduleItem;
import com.yube.redis.RedissonClientFactory;
import com.yube.utils.Validator;
import io.prometheus.client.Counter;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SchedulerBot extends Bot {

    private final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";
    private final static Pattern INNER_CONTENT_PATTERN = Pattern.compile("\".+\"");
    private final static Pattern SCHEDULE_TEXT_TAG_PATTERN = Pattern.compile("-t \"[a-zA-Z_0-9\\s]+\"");
    private final static Pattern SCHEDULE_DATE_TIME_TAG_PATTERN = Pattern.compile("-d \"\\d{4}-[01]\\d-[0-3]\\d\\s[0-2]\\d:[0-5]\\d\"");
    private final static Pattern SCHEDULE_REPEAT_COUNT_TAG_PATTERN = Pattern.compile("-r \"[1-9]\"");
    private final static Pattern SCHEDULE_INTERVAL_TAG_PATTERN = Pattern.compile("-i \"[1-9][0-9]{0,3}\"");
    private final static Pattern SCHEDULE_COMMAND_PATTERN = Pattern.compile("schedule -t \"[a-zA-Z_0-9\\s]+\" -d \"\\d{4}-[01]\\d-[0-3]\\d\\s[0-2]\\d:[0-5]\\d\"( -r \"[1-9]\" -i \"[1-9][0-9]{0,3}\")?");
    private final static Pattern SCHEDULE_ALL_COMMAND_PATTERN = Pattern.compile("schedule all");

    private static final Counter requests = Counter.build()
            .name("requests_total").help("Total requests").register();
    private static final Counter valid_requests = Counter.build()
            .name("valid_requests_total").help("Total valid requests").register();
    private static final Counter redis_requests = Counter.build()
            .name("redis_requests_total").help("Total Redis requests").register();

    protected SchedulerBot(String token, String botName) {
        super(token, botName);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                processScheduleItems();
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        executor.scheduleAtFixedRate(task, 30L, 10L, TimeUnit.SECONDS);
    }

    @Override
    public void onUpdateReceived(Update update) {
        requests.inc();
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String text = message.getText().trim();
            Matcher scheduleCommandMatcher = SCHEDULE_COMMAND_PATTERN.matcher(text);
            Matcher scheduleAllCommandMatcher = SCHEDULE_ALL_COMMAND_PATTERN.matcher(text);
            if (text.equals("ping")) {
                valid_requests.inc();
                String redisAddress = System.getenv("REDIS_ADDR");
                if (redisAddress != null) {
                    sendTextMessage(chatId, redisAddress);
                } else {
                    sendTextMessage(chatId, "pong");
                }
            } else if (scheduleCommandMatcher.matches()) {
                valid_requests.inc();
                try {
                    ScheduleItem scheduleItem = extractScheduleItem(chatId, text);
                    try {
                        RedissonClient client = RedissonClientFactory.getInstance().getRedissonClient();
                        RSet<ScheduleItem> inMemoryScheduleItems = client.getSet("scheduleItems");
                        inMemoryScheduleItems.add(scheduleItem);
                        sendTextMessage(chatId, String.format("New schedule item added:\n%s", scheduleItem.toString()));
                    } catch (Exception e) {
                        sendTextMessage(chatId, e.getMessage());
                        processException(e);
                    }
                } catch (CommandParseException e) {
                    sendTextMessage(chatId, String.format("Sorry, i can't process this command.\nReason: %s", e.getMessage()));
                    processException(e);
                }
            } else if (scheduleAllCommandMatcher.matches()) {
                valid_requests.inc();
                try {
                    RedissonClient client = RedissonClientFactory.getInstance().getRedissonClient();
                    RSet<ScheduleItem> inMemoryScheduleItems = client.getSet("scheduleItems");
                    List<ScheduleItem> schedules = inMemoryScheduleItems.stream()
                            .filter(si -> si.getChatId() == message.getChatId())
                            .sorted(Comparator.comparing(ScheduleItem::getTime))
                            .collect(Collectors.toList());
                    StringBuilder schedulesInfo = new StringBuilder();
                    if (!schedules.isEmpty()) {
                        for (ScheduleItem schedule : schedules) {
                            schedulesInfo.append(schedule.toString());
                        }
                        sendTextMessage(chatId, String.format("Your schedule items:\n%s", schedulesInfo.toString()));
                    } else {
                        sendTextMessage(chatId, "You haven't any active schedule items");
                    }
                } catch (Exception e) {
                    processException(e);
                }
            } else {
                sendTextMessage(chatId, "Sorry, i can't recognize this command");
            }
        }
    }

    private ScheduleItem extractScheduleItem(long chatId, String text) {
        ScheduleItem item = new ScheduleItem();
        item.setChatId(chatId);
        //Extracting text
        String textTag = extractTag(text, SCHEDULE_TEXT_TAG_PATTERN, true);
        String textContent = extractTagContent(textTag);
        item.setText(textContent);
        //Extracting time
        String dateTimeTag = extractTag(text, SCHEDULE_DATE_TIME_TAG_PATTERN, true);
        String dateTime = extractTagContent(dateTimeTag);
        if (Validator.validateDateTime(DATE_TIME_PATTERN, dateTime)) {
            LocalDateTime time = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
            if (time.isAfter(LocalDateTime.now())) {
                item.setTime(time);
            } else {
                throw new CommandParseException("Schedule time can't be less then current time");
            }
        } else {
            throw new CommandParseException(String.format("Time \"%s\" has invalid format", dateTime));
        }
        //Extracting repeat count
        String repeatCountTag = extractTag(text, SCHEDULE_REPEAT_COUNT_TAG_PATTERN, false);
        if (repeatCountTag != null) {
            String repeatCount = extractTagContent(repeatCountTag);
            item.setRepeatCount(Integer.parseInt(repeatCount));
        }
        //Extracting interval
        String intervalTag = extractTag(text, SCHEDULE_INTERVAL_TAG_PATTERN, false);
        if (intervalTag != null) {
            String interval = extractTagContent(intervalTag);
            item.setInterval(Integer.parseInt(interval));
        }
        return item;
    }

    private String extractTag(String text, Pattern tagPattern, boolean mandatory) {
        Matcher tagMatcher = tagPattern.matcher(text);
        if (tagMatcher.find()) {
            return tagMatcher.group();
        } else {
            if (mandatory) {
                throw new CommandParseException("Can't parse one of the mandatory tags");
            } else {
                return null;
            }
        }
    }

    private String extractTagContent(String text) {
        Matcher innerContentMatcher = INNER_CONTENT_PATTERN.matcher(text);
        if (innerContentMatcher.find()) {
            String content = innerContentMatcher.group().trim();
            return content.substring(1, content.length() - 1);
        }
        throw new CommandParseException(String.format("Can't extract content from tag %s", text));
    }

    private void processScheduleItems() {
        try {
            redis_requests.inc();
            RedissonClient client = RedissonClientFactory.getInstance().getRedissonClient();
            RSet<ScheduleItem> scheduleItems = client.getSet("scheduleItems");
            for (ScheduleItem scheduleItem : scheduleItems) {
                if (LocalDateTime.now().isAfter(scheduleItem.getTime())) {
                    sendTextMessage(scheduleItem.getChatId(), scheduleItem.getText());
                    scheduleItems.remove(scheduleItem);
                }
            }
        } catch (Exception e) {
            processException(e);
        }
    }
}
