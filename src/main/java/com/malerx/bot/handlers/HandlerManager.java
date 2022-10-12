package com.malerx.bot.handlers;

import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.factory.stm.StateFactory;
import com.malerx.bot.handlers.commands.CommandHandler;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
    private final Map<String, StateFactory> stateFactories;
    private final StateRepository stateRepository;

    public HandlerManager(Collection<CommandHandler> commands,
                          Collection<StateFactory> stateFactories,
                          StateRepository stateRepository) {
        this.commands = commands;
        this.stateRepository = stateRepository;
        this.stateFactories = stateFactories.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        (k) -> k.getClass().getSimpleName(),
                        (v) -> v)
                );
    }

    public CompletableFuture<Optional<Object>> handle(@NonNull Update update) {
        var message = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() :
                (update.hasMessage() ? update.getMessage() : null);
        if (Objects.nonNull(message)) {
            return stateRepository.findActiveProcess(message.getChatId(), Stage.PROCEED)
                    .thenCompose(states -> {
                        if (CollectionUtils.isNotEmpty(states)) {
                            var state = states.iterator().next();
                            return stateHandling(state, update);
                        }
                        return commandHandling(update);
                    });
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private CompletableFuture<Optional<Object>> stateHandling(PersistState state, Update update) {
        var factory = stateFactories.get(state.getStateMachine());
        if (Objects.nonNull(factory)) {
            var stateMachine = factory.createState(state, update);
            return stateMachine.nextStep();
        }
        return sendError(state);
    }

    private CompletableFuture<Optional<Object>> sendError(PersistState s) {
        s.setStage(Stage.ERROR);
        s.setDescription(
                String.format("Не найдена реализованная машина состояний для %s", s.getStateMachine())
        );
        return stateRepository.update(s)
                .thenApply(r -> Optional.of(
                        new SendMessage(
                                r.getChatId().toString(),
                                r.getDescription())
                ));
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
        var msg = new SendMessage(update.getMessage().getChatId().toString(),
                """
                        У вас нет начатых/незавершённых процессов.
                        Чтобы ознакомиться c доступными услугами введите
                                               
                        \t\t\t*/help*""");
        msg.enableMarkdown(Boolean.TRUE);
        return CompletableFuture.completedFuture(Optional.of(msg));
    }
}
