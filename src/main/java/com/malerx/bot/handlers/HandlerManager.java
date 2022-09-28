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
    private final Collection<UpdateHandler> handlers;

    public HandlerManager(Collection<UpdateHandler> handlers) {
        this.handlers = handlers;
    }

    public CompletableFuture<Optional<Object>> handle(@NonNull Update update) {
        for (UpdateHandler handler :
                handlers) {
            if (handler.support(update)) {
                return handler.handle(update);
            }
        }
        log.warn("handle() -> not support handle update {}", update.getMessage());
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
