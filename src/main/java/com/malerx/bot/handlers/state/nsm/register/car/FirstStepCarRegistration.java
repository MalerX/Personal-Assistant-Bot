package com.malerx.bot.handlers.state.nsm.register.car;

import com.malerx.bot.data.entity.Car;
import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.model.ButtonMessage;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.model.TextMessage;
import com.malerx.bot.data.repository.CarRepository;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.state.nsm.State;
import lombok.extern.slf4j.Slf4j;
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
    private final CarRepository carRepository;

    public FirstStepCarRegistration(Update update,
                                    PersistState state,
                                    TGUserRepository userRepository,
                                    StateRepository stateRepository,
                                    CarRepository carRepository) {
        this.message = update.getMessage();
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
        this.state = state;
        this.carRepository = carRepository;
    }

    @Override
    public CompletableFuture<Optional<OutgoingMessage>> next() {
        log.debug("next() -> first step register car");
        return findUser(message.getChatId())
                .thenCompose(user -> {
                    if (Objects.nonNull(user)) {
                        return createCar()
                                .thenCompose(c -> c.
                                        map(car -> addCar(user, car))
                                        .orElseGet(this::wrongFormat));
                    }
                    return userNotFound();
                });
    }

    private CompletableFuture<TGUser> findUser(Long id) {
        log.debug("findUser() -> find user with id {}", id);
        return userRepository.findById(id);
    }

    private CompletableFuture<Optional<Car>> createCar() {
        String[] carIfo = message.getText().split("\n");
        if (carIfo.length != 3) {
            log.error("createCar() -> wrong auto data format");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        log.debug("createCar() -> crete car from {}", Arrays.toString(carIfo));

        var car = new Car()
                .setModel(carIfo[0])
                .setColor(carIfo[1])
                .setRegNumber(carIfo[2]);
        return carRepository.save(car)
                .thenApply(Optional::of);
    }

    private CompletableFuture<Optional<OutgoingMessage>> addCar(TGUser user, Car car) {
        var tenant = user.getTenant();
        Set<Car> cars = new HashSet<>(user.getTenant().getCars());
        cars.add(car);
        tenant.setCars(cars);
        state.setStep(Step.TWO)
                .setDescription("Подтверждение ввода");
        return userRepository.update(user)
                .thenCompose(u -> stateRepository.update(state)
                        .thenApply(r -> {
                            var msg = new ButtonMessage("""
                                    Всё верно?%s""".formatted(car.toString()),
                                    Set.of(message.getChatId()),
                                    createKeyboard(car.getId()));
                            return Optional.of(msg);
                        }));
    }

    private ReplyKeyboard createKeyboard(Long carId) {
        var approve = InlineKeyboardButton.builder()
                .text("Да")
                .callbackData(YES)
                .build();
        var decline = InlineKeyboardButton.builder()
                .text("Нет")
                .callbackData(carId.toString())
                .build();
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(approve, decline))
                .build();
    }

    private CompletableFuture<Optional<OutgoingMessage>> wrongFormat() {
        return CompletableFuture.completedFuture(
                Optional.of(new TextMessage(Set.of(message.getChatId()), """
                        Введённые данные не соответствуют ожидаемому формату. Проверьте \
                        вводимые данные""")));
    }

    private CompletableFuture<Optional<OutgoingMessage>> userNotFound() {
        state.setStage(Stage.ERROR)
                .setDescription("""
                        Пользователь %d не зарегистрирован"""
                        .formatted(message.getChatId()));
        return stateRepository.update(state)
                .thenApply(r -> Optional.of(new TextMessage(
                        Set.of(message.getChatId()),
                        r.getDescription())));
    }
}