package com.malerx.bot.handlers.state.nsm.register.car;

import com.malerx.bot.data.entity.Car;
import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.state.nsm.State;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class FirstStepCarRegistration implements State {
    private static final String YES = "Yes";
    private static final String NO = "No";

    private final Message message;
    private final PersistState state;

    private final TGUserRepository userRepository;
    private final StateRepository stateRepository;

    public FirstStepCarRegistration(Update update,
                                    PersistState state,
                                    TGUserRepository userRepository,
                                    StateRepository stateRepository) {
        this.message = update.getMessage();
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
        this.state = state;
    }

    @Override
    public CompletableFuture<Optional<Object>> nextStep() {
        log.debug("nextStep() -> first step register car");
        return findUser(message.getChatId())
                .thenCompose(user -> {
                    if (Objects.nonNull(user)) {
                        var carOpt = createCar();
                        return carOpt
                                .map(car -> addCar(user, car))
                                .orElseGet(this::wrongFormat);
                    }
                    return userNotFound();
                });
    }

    private CompletableFuture<TGUser> findUser(Long id) {
        log.debug("findUser() -> find user with id {}", id);
        return userRepository.findById(id);
    }

    private Optional<Car> createCar() {
        String[] carIfo = message.getText().split("\n");
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

    private CompletableFuture<Optional<Object>> addCar(TGUser user, Car car) {
        var tenant = user.getTenant();
        Set<Car> cars = new HashSet<>(user.getTenant().getCars());
        cars.add(car);
        tenant.setCars(cars);
        state.setStep(Step.TWO)
                .setDescription("Подтверждение ввода");
        return stateRepository.update(state)
                .thenApply(r -> {
                    var msg = new SendMessage(message.getChatId().toString(), """
                            Всё верно?%s""".formatted(car.toString()));
                    msg.setReplyMarkup(createKeyboard());
                    return Optional.of(msg);
                });
    }

    private ReplyKeyboard createKeyboard() {
        var approve = InlineKeyboardButton.builder()
                .text("Да")
                .callbackData(YES)
                .build();
        var decline = InlineKeyboardButton.builder()
                .text("Нет")
                .callbackData(NO)
                .build();
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(approve, decline))
                .build();
    }

    private CompletableFuture<Optional<Object>> wrongFormat() {
        return CompletableFuture.completedFuture(
                Optional.of(new SendMessage(message.getChatId().toString(), """
                        Введённые данные не соответствуют ожидаемому формату. Проверьте \
                        вводимые данные""")));
    }

    private CompletableFuture<Optional<Object>> userNotFound() {
        state.setStage(Stage.ERROR)
                .setMessage("""
                        Пользователь %d не зарегистрирован"""
                        .formatted(message.getChatId()));
        return stateRepository.update(state)
                .thenApply(r -> Optional.of(new SendMessage(
                        message.getChatId().toString(),
                        r.getDescription())));
    }
}