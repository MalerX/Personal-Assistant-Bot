package com.malerx.bot.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.model.CallbackData;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.model.TextMessage;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.factory.stm.StateFactory;
import com.malerx.bot.handlers.commands.CommandHandler;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class HandlerManager {
    private final Collection<CommandHandler> commands;
    private final Map<String, StateFactory> stateFactories;
    private final StateRepository stateRepository;
    private final ObjectMapper mapper;

    public HandlerManager(Collection<CommandHandler> commands,
                          Collection<StateFactory> stateFactories,
                          StateRepository stateRepository,
                          ObjectMapper mapper) {
        this.commands = commands;
        this.stateRepository = stateRepository;
        this.stateFactories = stateFactories.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        (k) -> k.getClass().getSimpleName(),
                        (v) -> v)
                );
        this.mapper = mapper;
    }

    public CompletableFuture<Optional<OutgoingMessage>> handle(@NonNull Update update) {
        if (update.hasMessage() && update.getMessage().getText().startsWith("/")) {
            return commandHandling(update);
        } else {
            return findState(update);
        }
    }

    private CompletableFuture<Optional<OutgoingMessage>> commandHandling(@NonNull Update update) {
        for (CommandHandler handler :
                commands) {
            if (handler.support(update)) {
                return handler.handle(update);
            }
        }
        log.error("commandHandling() -> not found handler for {} command", update.getMessage().getText());
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private CallbackData parseCallbackDate(final Update update) {
        try {
            return mapper.readValue(update.getCallbackQuery().getData(), CallbackData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Optional<OutgoingMessage>> findState(Update update) {
        if (update.hasCallbackQuery()) {
            var callback = parseCallbackDate(update);
            return stateRepository.findById(callback.getStateId())
                    .thenCompose(state -> {
                        if (state != null) {
                            return stateHandling(state, update);
                        }
                        log.error("findState() -> not found state {}", callback.getStateId());
                        return notFoundState(update.getCallbackQuery().getFrom().getId());
                    });
        } else {
            var chatId = update.getMessage().getChatId();
            return stateRepository.findActiveProcess(chatId, Stage.PROCEED)
                    .thenCompose(states -> {
                        if (states != null) {
                            if (states.size() > 1)
                                log.warn("findState() -> for user {} found more then 1 state", chatId);
                            return stateHandling(states.iterator().next(), update);
                        }
                        log.error("findState() -> not found state for user {}", chatId);
                        return notFoundState(chatId);
                    });
        }
    }

    CompletableFuture<Optional<OutgoingMessage>> stateHandling(final PersistState state, final Update update) {
        log.debug("stateHandling() -> handling state {}", state);
        var factory = stateFactories.get(state.getStateMachine());
        if (Objects.nonNull(factory)) {
            var stateMachine = factory.createState(state, update);
            return stateMachine.next();
        }
        return sendError(state);
    }

    private CompletableFuture<Optional<OutgoingMessage>> sendError(PersistState s) {
        s.setStage(Stage.ERROR);
        s.setDescription(
                String.format("Не найдена реализованная машина состояний для %s", s.getStateMachine())
        );
        return stateRepository.update(s)
                .thenApply(r -> Optional.of(
                        new TextMessage(
                                Set.of(r.getChatId()),
                                r.getDescription())
                ));
    }

    private CompletableFuture<Optional<OutgoingMessage>> notFoundState(final Long chatId) {
        return CompletableFuture.supplyAsync(() -> {
            log.warn("commandHandling() -> fail handle update from user {}", chatId);
            var msg = new TextMessage(Set.of(chatId),
                    """
                            У вас нет начатых/незавершённых процессов.
                            Чтобы ознакомиться c доступными услугами введите
                                                   
                            \t\t\t*/help*""");
            return Optional.of(msg);
        });
    }
}
