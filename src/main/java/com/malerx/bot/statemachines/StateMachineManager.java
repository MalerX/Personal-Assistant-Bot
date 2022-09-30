package com.malerx.bot.statemachines;

import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class StateMachineManager {

    public CompletableFuture<Optional<Object>> stateHandling(@NonNull final Update update) {
        log.info("stateHandling() -> not implements state machine");
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
