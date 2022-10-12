package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.commands.CommandHandler;
import com.malerx.bot.handlers.state.impl.CarRegistration;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class StartRegisterCar implements CommandHandler {
    private static final String COMMAND = "/reg_car";
    private final StateRepository stateRepository;
    private final TGUserRepository userRepository;

    public StartRegisterCar(StateRepository stateRepository,
                            TGUserRepository userRepository) {
        this.stateRepository = stateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CompletableFuture<Optional<Object>> handle(@NonNull final Update update) {
        log.debug("handle() -> init process registration car for user {}", update.getMessage().getChatId());
        return userRepository.existsById(update.getMessage().getChatId())
                .thenCompose(exist -> {
                    if (!exist)
                        return CompletableFuture.completedFuture(
                                Optional.of(
                                        createMsg(update, """
                                                Вы ещё не зарегистрированы. Пройдите регистрацию по команде\
                                                */registration*""")));
                    return stateRepository.save(createState(update))
                            .thenApply(r -> Optional.of(createMsg(update,
                                    """
                                            Введите информацию об автомобиле в следующем формате:
                                            *модель
                                            цвет
                                            номер гос регистрации*
                                            """)));
                });
    }

    private PersistState createState(final Update update) {
        return new PersistState()
                .setChatId(update.getMessage().getChatId())
                .setDescription("Регистрация автомобиля в системе бота")
                .setStateMachine(CarRegistration.class.getSimpleName())
                .setStage(Stage.PROCEED)
                .setStep(Step.ONE);
    }

    private SendMessage createMsg(final Update update, String text) {

        var message = new SendMessage(
                update.getMessage().getChatId().toString(),
                text);
        message.enableMarkdown(Boolean.TRUE);
        return message;
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
