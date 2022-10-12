package com.malerx.bot.handlers.state.impl;

import com.malerx.bot.data.entity.Car;
import com.malerx.bot.data.entity.PersistState;
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
import org.telegram.telegrambots.meta.api.objects.Message;
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
        var message = op.update().getMessage();
        return findUser(op.state().getChatId())
                .thenCompose(user -> {
                    if (Objects.nonNull(user)) {
                        var tenant = user.getTenant();
                        var car = createCar(message);
                        if (car.isEmpty()) {
                            return CompletableFuture.completedFuture(Optional.of(createMsg(message,
                                    """
                                            Введённые данные не соответствуют ожидаемому формату. Проверьте \
                                            вводимые данные""")));
                        }
                        Set<Car> cars = new HashSet<>(user.getTenant().getCars());
                        cars.add(car.get());
                        tenant.setCars(cars);
                        var s = op.state()
                                .setStep(Step.TWO);
                        return userRepository.update(user)
                                .thenCombine(updateState(s), (u, us) -> {
                                    var checkInputMsg = """
                                            Всё верно?%s""".formatted(car.get().toString());
                                    return Optional.of(createAnswer(message, checkInputMsg));
                                });
                    }
                    log.error("one() -> not found user with idop.persistState()");
                    var s = op.state()
                            .setStage(Stage.ERROR)
                            .setMessage(createMsg(message,
                                    "Not found user wit ID %d".formatted(message.getChatId())));
                    return stateRepository.update(s)
                            .thenApply(r -> Optional.of(s.toMessage()));
                });
    }

    private CompletableFuture<TGUser> findUser(Long id) {
        log.debug("findUser() -> find user with id {}", id);
        return userRepository.findById(id);
    }

    private SendMessage createAnswer(Message m, String text) {
        var msg = createMsg(m, text);
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

    private Optional<Car> createCar(final Message m) {
        String[] carIfo = m.getText().split("\n");
        if (carIfo.length != 3) {
            log.error("createCar() -> wrong auto data format");
            return Optional.empty();
        }
        log.debug("createCar() -> crete car from {}", Arrays.toString(carIfo));
        return Optional.of(new Car()
                .setModel(carIfo[0])
                .setColor(carIfo[1])
                .setRegNumber(carIfo[2]));
    }

    private CompletableFuture<Optional<Object>> two(Operation op) {
        var command = op.update().getMessage().getText();
        final PersistState s;
        if (Objects.equals(YES, command)) {
            s = op.state()
                    .setStage(Stage.DONE)
                    .setMessage("""
                            Автомобиль успешно зарегистрирована""");
        } else if (Objects.equals(NO, command)) {
            s = op.state()
                    .setStep(Step.ONE)
                    .setMessage("""
                            Введите корректную информацию об автомобиле разделённую по строкам:
                                                        
                            *модель
                            цвет
                            номер год регистрации*""");
        } else {
            log.error("two() -> undefine command {} for this stage", command);
            s = op.state()
                    .setStage(Stage.ERROR)
                    .setMessage("Данная команда не определена. Попробуйте снова.");
        }
        return updateState(s).thenApply(r -> Optional.of(s.toMessage()));
    }

    private SendMessage createMsg(Message m, String text) {
        var message = new SendMessage(m.getChatId().toString(), text);
        message.enableMarkdown(Boolean.TRUE);
        return message;
    }

    private CompletableFuture<PersistState> updateState(PersistState s) {
        log.debug("updateState() -> update persistState {}", s);
        return stateRepository.update(s);
    }
}
