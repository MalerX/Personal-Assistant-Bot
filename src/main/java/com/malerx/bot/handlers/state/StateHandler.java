package com.malerx.bot.handlers.state;

import com.malerx.bot.handlers.Operation;
import io.micronaut.core.annotation.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Общий интерфейс для процессов конечных автоматов.
 */
public interface StateHandler {

    @NonNull
    CompletableFuture<Optional<Object>> proceed(@NonNull Operation operation);
}
