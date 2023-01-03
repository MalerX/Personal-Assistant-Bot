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
        if (update.hasMessage() && update.getMessage().getText().startsWith("/")
                || update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("/")) {
            return commandHandling(update);
        } else if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("{")) {
            var callback = parseCallbackDate(update);
            return stateRepository.findById(callback.getStateId())
                    .thenCompose(state -> {
                        if (state != null) {
                            return stateHandling(state, update);
                        }
                        log.error("handle() -> not found state {}", callback.getStateId());
                        return CompletableFuture.completedFuture(Optional.empty());
                    });
        } else {
            long chatId = update.hasCallbackQuery() ? update.getCallbackQuery().getFrom().getId()
                    : update.hasMessage() ? update.getMessage().getChatId() : -1L;
            if (chatId != -1L) {
                return stateRepository.findActiveProcess(chatId, Stage.PROCEED)
                        .thenCompose(states -> {
                            if (states != null) {
                                return stateHandling(states.stream().findFirst().orElseThrow(), update);
                            }
                            log.error("handle() -> not found state for {}", chatId);
                            return CompletableFuture.completedFuture(Optional.empty());
                        });
            }
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    private CallbackData parseCallbackDate(final Update update) {
        try {
            return mapper.readValue(update.getCallbackQuery().getData(), CallbackData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Optional<OutgoingMessage>> stateHandling(PersistState state, Update update) {
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

    private CompletableFuture<Optional<OutgoingMessage>> commandHandling(@NonNull Update update) {
        for (CommandHandler handler :
                commands) {
            if (handler.support(update)) {
                return handler.handle(update);
            }
        }
        log.warn("commandHandling() -> not support handle update {}", update.getMessage());
        var msg = new TextMessage(Set.of(update.getMessage().getChatId()),
                """
                        У вас нет начатых/незавершённых процессов.
                        Чтобы ознакомиться c доступными услугами введите
                                               
                        \t\t\t*/help*""");
        return CompletableFuture.completedFuture(Optional.of(msg));
    }
}
