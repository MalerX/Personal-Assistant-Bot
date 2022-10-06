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
                .setId(update.getMessage().getChatId())
                .setStateMachine(RegisterStateMachine.class.getName())
                .setStep(Step.ONE)
                .setStage(Stage.PROCEED)
                .setDescription("""
                        Регистрация жителя МКД в системе жилого комплекса""")
                .setMessage("""
                        Введите ваши имя и фамилию:"""
                );
        return stateRepository.save(state)
                .thenApply(s -> Optional.of(
                        new SendMessage(s.getId().toString(), s.getMessage())));
    }

    @Override
    public Boolean support(@NonNull Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
