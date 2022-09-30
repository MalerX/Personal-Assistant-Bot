package com.malerx.bot.handlers;

import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class HandlerManager {
    private final Collection<CommandHandler> handlers;

    public HandlerManager(Collection<CommandHandler> handlers) {
        this.handlers = handlers;
    }

    public CompletableFuture<Optional<Object>> commandHandling(@NonNull Update update) {
        for (CommandHandler handler :
                handlers) {
            if (handler.support(update)) {
                return handler.handle(update);
            }
        }
        log.warn("commandHandling() -> not support commandHandling update {}", update.getMessage());
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
