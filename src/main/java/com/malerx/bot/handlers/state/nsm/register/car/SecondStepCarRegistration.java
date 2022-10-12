package com.malerx.bot.handlers.state.nsm.register.car;

import com.malerx.bot.data.entity.Car;
import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.repository.CarRepository;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.handlers.state.nsm.State;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SecondStepCarRegistration implements State {
    private static final String YES = "Yes";

    private final Message message;
    private final CallbackQuery callbackQuery;
    private final PersistState state;

    private final StateRepository stateRepository;
    private final CarRepository carRepository;

    public SecondStepCarRegistration(Update update,
                                     PersistState state,
                                     StateRepository stateRepository,
                                     CarRepository carRepository) {
        if (update.hasCallbackQuery()) {
            this.message = update.getCallbackQuery().getMessage();
            this.callbackQuery = update.getCallbackQuery();
        } else
            throw new RuntimeException("Wrong input data");

        this.state = state;
        this.stateRepository = stateRepository;
        this.carRepository = carRepository;
    }

    @Override
    public CompletableFuture<Optional<Object>> nextStep() {
        if (Objects.equals(YES, callbackQuery.getData()))
            return ok();
        else
            return rEdit();
    }

    private CompletableFuture<Optional<Object>> ok() {
        state.setStage(Stage.DONE)
                .setDescription("Ввод корректный. Автомобиль добавлен");
        return stateRepository.update(state)
                .thenApply(r -> Optional.of(new SendMessage(message.getChatId().toString(), r.getDescription())));
    }

    private CompletableFuture<Optional<Object>> rEdit() {
        var carId = Long.parseLong(callbackQuery.getData());
        return carRepository.findById(carId)
                .thenCompose(car -> disableCar(car)
                        .thenCombine(updateState(), (v, s) -> s));
    }

    private CompletableFuture<Optional<Object>> updateState() {
        state.setStep(Step.ONE)
                .setDescription("""
                        Введите информацию об автомобиле в следующем формате:
                        *модель
                        цвет
                        номер гос регистрации*
                        """);
        return stateRepository.update(state)
                .thenApply(r -> {
                    var msg = new SendMessage(message.getChatId().toString(), r.getDescription());
                    msg.enableMarkdown(Boolean.TRUE);
                    return Optional.of(msg);
                });
    }

    private CompletableFuture<Void> disableCar(Car c) {
        c.setActive(Boolean.FALSE);
        return carRepository.update(c)
                .thenAccept(r -> log.debug("disableCar() -> success disabled car {}", r));
    }
}
