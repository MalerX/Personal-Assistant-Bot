package com.malerx.bot.service.handlers.impl;

import com.malerx.bot.service.handlers.UpdateHandler;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class HelpHandler implements UpdateHandler {
    @Override
    public CompletableFuture<Optional<Object>> handle(Update update) {
        return CompletableFuture.supplyAsync(() -> Optional.of(
                new SendMessage(
                        update.getMessage().getChatId().toString(),
                        "Тестовый бот, функционал находится в стадии разработки."
                )
        ));
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith("/help");
    }
}
