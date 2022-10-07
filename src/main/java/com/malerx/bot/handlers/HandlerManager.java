package com.malerx.bot.handlers;

import com.malerx.bot.data.entity.State;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.handlers.commands.CommandHandler;
import com.malerx.bot.handlers.state.StateHandler;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class HandlerManager {
    private final Collection<CommandHandler> commands;
    private final Map<String, StateHandler> stateMachines;

    private final StateRepository stateRepository;

    public HandlerManager(Collection<CommandHandler> commands,
                          Collection<StateHandler> states,
                          StateRepository stateRepository) {
        this.commands = commands;
        this.stateMachines = states.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(m -> m.getClass().getName(), m -> m));
        this.stateRepository = stateRepository;
    }

    public CompletableFuture<Optional<Object>> handle(@NonNull Update update) {
        log.debug("handle() -> find started process for {}", update.getMessage().getChatId());
        return stateRepository.findByIdByStage(update.getMessage().getChatId(), Stage.PROCEED)
                .thenCompose(states -> {
                    if (CollectionUtils.isNotEmpty(states)) {
                        if (states.size() > 1)
                            log.warn("handle() -> found more than one states for tg: {}", update.getMessage().getChatId());
                        var state = states.iterator().next();
                        return stateHandling(
                                new Operation(update, state));
                    }
                    log.debug("handle() -> not found started state");
                    return commandHandling(update);
                });
    }

    private CompletableFuture<Optional<Object>> commandHandling(@NonNull Update update) {
        log.debug("commandHandling() -> handle command");
        for (CommandHandler handler :
                commands) {
            if (handler.support(update)) {
                return handler.handle(update);
            }
        }
        log.warn("handle() -> not support handle update {}", update.getMessage());
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private CompletableFuture<Optional<Object>> stateHandling(@NonNull Operation operation) {
        log.debug("stateHandling() -> found state for {}, \nupdate: {}",
                operation.state(), operation.update().getMessage());
        StateHandler handler = stateMachines.get(operation.state().getStateMachine());
        log.debug("stateHandling() -> get machine {}", handler);
        return handler.proceed(operation)
                .thenCompose(this::updateState)
                .thenApply(r -> r.map(State::toMessage));
    }

    private CompletableFuture<Optional<State>> updateState(State state) {
        return stateRepository.update(state)
                .thenApply(Optional::of);
    }
}
