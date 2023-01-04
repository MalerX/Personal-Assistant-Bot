package com.malerx.bot.handlers.commands.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.model.ButtonMessage;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.model.TextMessage;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.factory.stm.GettingPassStateFactory;
import com.malerx.bot.handlers.commands.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class PassHandler implements CommandHandler {
    private final static String COMMAND = "/pass";

    private final TGUserRepository userRepository;
    private final StateRepository stateRepository;
    private final ObjectMapper mapper;

    public PassHandler(TGUserRepository userRepository, StateRepository stateRepository, ObjectMapper mapper) {
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
        this.mapper = mapper;
    }

    @Override
    public CompletableFuture<Optional<OutgoingMessage>> handle(Update update) {
        var chatId = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getChatId() :
                (update.hasMessage() ? update.getMessage().getChatId() : null);
        if (chatId != null) {
            return isRegistered(chatId)
                    .thenCompose(isRegistered -> {
                        if (isRegistered) {
                            return createAnswer(chatId);
                        }
                        return notRegistered(chatId);
                    });
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private CompletableFuture<Boolean> isRegistered(long chatId) {
        log.debug("isRegistered() -> check user {} on register", chatId);
        return userRepository.existsById(chatId);
    }

    private CompletableFuture<Optional<OutgoingMessage>> createAnswer(long chatId) {
        return createState(chatId)
                .thenApply(s -> {
                    var msg = "Введите следующую информацию:\n\nмодель\nрегистрационный номер";
                    OutgoingMessage m = new TextMessage(Set.of(chatId), msg);
                    return Optional.of(m);
                });
    }

    private CompletableFuture<PersistState> createState(long chatId) {
        var state = new PersistState()
                .setStage(Stage.PROCEED)
                .setChatId(chatId)
                .setStep(Step.ONE)
                .setDescription("Получение временного пропуска")
                .setStateMachine(GettingPassStateFactory.class.getSimpleName());
        return stateRepository.save(state);
    }

    private CompletableFuture<Optional<OutgoingMessage>> notRegistered(long chatId) {
        OutgoingMessage message = new ButtonMessage("Для получения пропуска необходмо быть зарегистрированным в системе",
                Set.of(chatId),
                InlineKeyboardMarkup.builder()
                        .keyboardRow(List.of(InlineKeyboardButton.builder()
                                .text("Зарегистрироваться в системе")
                                .callbackData("/register")
                                .build()))
                        .build());
        return CompletableFuture.completedFuture(Optional.of(message));
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
