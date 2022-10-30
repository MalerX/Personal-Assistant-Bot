package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.model.TextMessage;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.factory.stm.RegisterStateFactory;
import com.malerx.bot.handlers.commands.CommandHandler;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class RegisterHandler implements CommandHandler {
    private final static String COMMAND = "/register";

    private final StateRepository stateRepository;
    private final TGUserRepository userRepository;

    public RegisterHandler(StateRepository stateRepository,
                           TGUserRepository userRepository) {
        this.stateRepository = stateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CompletableFuture<Optional<OutgoingMessage>> handle(@NonNull Update update) {
        var chatId = update.getMessage().getChatId();
        return userRepository.existsById(chatId)
                .thenCompose(exist -> {
                    if (exist) {
                        log.debug("handle() -> user already register");
                        return CompletableFuture.completedFuture(Optional.of(
                                createMsg(update,
                                        """
                                                Вы уже зарегистрированы в системе бота.""")));
                    } else {
                        log.debug("handle() -> handle request of registration");
                        return startRegistration(update);
                    }
                });
    }

    CompletableFuture<Optional<OutgoingMessage>> startRegistration(Update u) {
        PersistState persistState = new PersistState()
                .setChatId(u.getMessage().getChatId())
                .setStateMachine(RegisterStateFactory.class.getSimpleName())
                .setStep(Step.ONE)
                .setStage(Stage.PROCEED)
                .setDescription("""
                        Регистрация пользователя в системе бота""");
        return stateRepository.save(persistState)
                .thenApply(s -> Optional.of(createMsg(u, "Введите ваши имя и фамилию:")));

    }

    private OutgoingMessage createMsg(Update update, String s) {
        return new TextMessage(Set.of(update.getMessage().getChatId()), s);
    }

    @Override
    public Boolean support(@NonNull Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
