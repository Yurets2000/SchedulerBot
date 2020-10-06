package com.yube;

import com.yube.bot.Bot;
import com.yube.bot.BotFactory;
import com.yube.utils.log.RestLogger;
import com.yube.utils.log.LogLevel;
import org.telegram.telegrambots.ApiContextInitializer;

public class Main {

    private final static RestLogger logger = RestLogger.getInstance();

    public static void main(String[] args) {
        ApiContextInitializer.init();
        Bot bot = BotFactory.getBot(
                "scheduler",
                "1368033086:AAEysbj67R48EGQpJjPvj-frvA9c_GDocsU",
                "ActsSchedulerBot");
        Bot.runBot(bot);
        logger.log(LogLevel.INFO, "Bot started successfully");
//        if (args == null || args.length != 3) {
//            log.error("You must run bot with 3 args - BotType, BotToken and BotName");
//        } else {
//            ApiContextInitializer.init();
//            Bot bot = BotFactory.getBot(args[0], args[1], args[2]);
//            Bot.runBot(bot);
//            log.info("Bot started successfully");
//        }
    }
}
