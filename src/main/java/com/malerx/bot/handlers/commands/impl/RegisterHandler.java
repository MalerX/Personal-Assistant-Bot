package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.factory.stm.RegisterStateFactory;
import com.malerx.bot.handlers.commands.CommandHandler;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Optional;
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
    public CompletableFuture<Optional<Object>> handle(@NonNull Update update) {
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

    CompletableFuture<Optional<Object>> startRegistration(Update u) {
        PersistState persistState = new PersistState()
                .setChatId(u.getMessage().getChatId())
                .setStateMachine(RegisterStateFactory.class.getSimpleName())
                .setStep(Step.ONE)
                .setStage(Stage.PROCEED)
                .setDescription("""
                        Регистрация пользователя в системе бота""")
                .setMessage(createMsg(u,
                        """
                                Введите ваши имя и фамилию:"""));
        return stateRepository.save(persistState)
                .thenApply(s -> Optional.of(s.getMessage()));

    }

    private Object createMsg(Update update, String s) {
        var msg = new SendMessage(
                update.getMessage().getChatId().toString(),
                s);
        msg.enableMarkdown(Boolean.TRUE);
        return msg;
    }

    @Override
    public Boolean support(@NonNull Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
