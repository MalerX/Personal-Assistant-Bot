package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.entity.State;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.handlers.commands.CommandHandler;
import com.malerx.bot.handlers.state.impl.RegisterStateMachine;
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

    public RegisterHandler(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @Override
    public CompletableFuture<Optional<Object>> handle(@NonNull Update update) {
        log.debug("handle() -> handle request of registration");
        State state = new State()
                .setChatId(update.getMessage().getChatId())
                .setStateMachine(RegisterStateMachine.class.getSimpleName())
                .setStep(Step.ONE)
                .setStage(Stage.PROCEED)
                .setDescription("""
                        Регистрация пользователя в системе бота""")
                .setMessage(createMsg(update));
        return stateRepository.save(state)
                .thenApply(s -> Optional.of(s.getMessage()));
    }

    private Object createMsg(Update update) {
        return new SendMessage(
                update.getMessage().getChatId().toString(),
                """
                        Введите ваши имя и фамилию:""");
    }

    @Override
    public Boolean support(@NonNull Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
