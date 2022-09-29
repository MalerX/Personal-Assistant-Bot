package com.malerx.bot.handlers.impl;

import com.malerx.bot.handlers.CommandHandler;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class HelpHandler implements CommandHandler {
    private static final String COMMAND = "/help";

    @Override
    public CompletableFuture<Optional<Object>> handle(@NonNull Update update) {
        return CompletableFuture.supplyAsync(() -> Optional.of(
                new SendMessage(
                        update.getMessage().getChatId().toString(),
                        "Тестовый бот, функционал находится в стадии разработки."
                )));
    }

    @Override
    public Boolean support(@NonNull Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
