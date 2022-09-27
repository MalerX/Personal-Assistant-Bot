package com.malerx;

import com.malerx.bot.AssistantBot;
import io.micronaut.runtime.Micronaut;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
