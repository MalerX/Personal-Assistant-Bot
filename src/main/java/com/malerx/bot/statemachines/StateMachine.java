package com.malerx.bot.statemachines;

import io.micronaut.core.annotation.NonNull;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Общий интерфейс для процессов конечных автоматов.
 */
public interface StateMachine {

    CompletableFuture<Optional<Object>> handle(@NonNull final Update update);
}
