package com.malerx.bot.handlers.state.impl;

import com.malerx.bot.data.entity.Car;
import com.malerx.bot.data.entity.State;
import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.Operation;
import com.malerx.bot.handlers.state.StateHandler;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class CarRegistration implements StateHandler {
    private static final String YES = "Yes";
    private static final String NO = "No";
    private final TGUserRepository userRepository;
    private final StateRepository stateRepository;

    public CarRegistration(TGUserRepository userRepository,
                           StateRepository stateRepository) {
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
    }

    @Override
    @NonNull
    public CompletableFuture<Optional<Object>> proceed(@NonNull Operation operation) {
        var state = operation.state();
        switch (state.getStep()) {
            case ONE -> {
                return one(operation);
            }
            case TWO -> {
                return two(operation);
            }
        }
        return CompletableFuture.completedFuture(Optional.of(
                new SendMessage()
        ));
    }

    private CompletableFuture<Optional<Object>> one(Operation op) {
        return findUser(op.state().getChatId())
                .thenCompose(user -> {
                    if (Objects.nonNull(user)) {
                        var tenant = user.getTenant();
                        var car = createCar(op.update());
                        Set<Car> cars = new HashSet<>(user.getTenant().getCars());
                        cars.add(car);
                        tenant.setCars(cars);
                        return userRepository.update(user)
                                .thenCombine(updateState(op.state()), (u, s) -> {
                                    var checkInputMsg = """
                                            Всё верно?
                                            %s""".formatted(car.toString());
                                    return Optional.of(createAnswer(op, checkInputMsg));
                                });
                    }
                    log.error("one() -> not found user with idop.state()");
                    var s = op.state()
                            .setStage(Stage.ERROR)
                            .setMessage(createMsg(op, "Not found user wit ID %d".formatted(op.state().getChatId())));
                    return stateRepository.update(s)
                            .thenApply(r -> Optional.of(s.toMessage()));
                });
    }

    private CompletableFuture<TGUser> findUser(Long id) {
        log.debug("findUser() -> find user with id {}", id);
        return userRepository.findById(id);
    }

    private SendMessage createAnswer(Operation o, String text) {
        var msg = createMsg(o, text);
        msg.setReplyMarkup(createKeyboard());
        return msg;
    }

    private ReplyKeyboard createKeyboard() {
        var approve = KeyboardButton.builder()
                .text(YES)
                .build();
        var decline = KeyboardButton.builder()
                .text(NO)
                .build();
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(Boolean.TRUE)
                .oneTimeKeyboard(Boolean.TRUE)
                .keyboardRow(new KeyboardRow(
                        List.of(approve, decline)
                ))
                .build();
    }

    private Car createCar(final Update update) {
        String[] carIfo = update.getMessage().getText().split("\n");
        log.debug("createCar() -> crete car from {}", Arrays.toString(carIfo));
        return new Car()
                .setModel(carIfo[0])
                .setColor(carIfo[1])
                .setRegNumber(carIfo[2]);
    }

    private CompletableFuture<Optional<Object>> two(Operation op) {
        var command = op.update().getMessage().getText();
        if (Objects.equals(YES, command)) {
            var s = op.state()
                    .setStage(Stage.DONE)
                    .setMessage("""
                            Машина успешно зарегистрирована""");
            return updateState(s).thenApply(r -> Optional.of(s.toMessage()));
        } else if (Objects.equals(NO, command)) {
            op.state()
                    .setStep(Step.ONE)
                    .setMessage("""
                            Введите заново информацию об автомобиле разделённую по строкам:
                            ***модель
                            цвет
                            номер год регистрации***""");
        }
        log.error("two() -> undefine command {} fro this stage", command);
        var s = op.state()
                .setStage(Stage.ERROR)
                .setMessage("Данная команда не определена. Попробуйте снова.");
        return updateState(s).thenApply(r -> Optional.of(s.toMessage()));
    }

    private SendMessage createMsg(Operation op, String text) {
        var message = new SendMessage(op.state().getChatId().toString(), text);
        message.enableMarkdown(Boolean.TRUE);
        return message;
    }

    private CompletableFuture<State> updateState(State s) {
        log.debug("updateState() -> update state {}", s);
        return stateRepository.update(s);
    }
}
