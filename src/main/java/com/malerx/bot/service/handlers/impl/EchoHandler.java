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
public class EchoHandler implements UpdateHandler {
    private static final String COMMAND = "!e ";

    @Override
    public CompletableFuture<Optional<Object>> handle(Update update) {
        log.debug("handle() -> handle message {}", update.getMessage());
        return CompletableFuture.supplyAsync(() -> Optional.of(
                new SendMessage(
                        update.getMessage().getChatId().toString(),
                        "Echo: ".concat(update.getMessage().getText().substring(3).trim())
                )));
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
