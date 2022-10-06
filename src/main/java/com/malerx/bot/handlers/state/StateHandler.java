package com.malerx.bot.handlers.state;

import com.malerx.bot.data.entity.State;
import com.malerx.bot.handlers.Operation;
import io.micronaut.core.annotation.NonNull;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Общий интерфейс для процессов конечных автоматов.
 */
public interface StateHandler {

    @NonNull
    CompletableFuture<State> proceed(@NonNull Operation operation);
}
