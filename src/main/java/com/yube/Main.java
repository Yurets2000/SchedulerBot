package com.yube;

import com.yube.bot.Bot;
import com.yube.bot.BotFactory;
import io.prometheus.client.exporter.HTTPServer;
import org.telegram.telegrambots.ApiContextInitializer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        //start exposing prometheus metrics on 9091
        HTTPServer server = new HTTPServer(9091);
        //start Telegram bot
        ApiContextInitializer.init();
        Bot bot = BotFactory.getBot(
                "scheduler",
                "1368033086:AAEysbj67R48EGQpJjPvj-frvA9c_GDocsU",
                "ActsSchedulerBot");
        Bot.runBot(bot);

//        Use when parameters should be passed
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
