package com.malerx.bot.handlers.state.impl;

import com.malerx.bot.data.entity.Address;
import com.malerx.bot.data.entity.State;
import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.entity.Tenant;
import com.malerx.bot.data.enums.Role;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.Operation;
import com.malerx.bot.handlers.state.StateHandler;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class RegisterStateMachine implements StateHandler {
    private final TGUserRepository userRepository;

    public RegisterStateMachine(TGUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @NonNull
    public CompletableFuture<State> proceed(@NonNull Operation operation) {
        log.debug(("proceed() -> "));
        switch (operation.state().getStep()) {
            case ONE -> {
                return one(operation);
            }
            case TWO -> {
                return two(operation);
            }
        }
        return CompletableFuture.completedFuture(
                new State()
                        .setId(operation.update().getMessage().getChatId())
                        .setStateMachine(this.getClass().getName())
                        .setStage(Stage.ERROR)
                        .setMessage("Этап не предусмотрен выполнением настоящей операции"));
    }

    private CompletableFuture<State> one(Operation operation) {
        Update update = operation.update();
        String[] firstSecond = update.getMessage().getText().split("\s");
        TGUser user = new TGUser()
                .setId(update.getMessage().getChatId())
                .setRole(Role.TENANT);
        return userRepository.save(user)
                .thenApply(u -> operation.state()
                        .setStep(Step.TWO)
                        .setMessage("""
                                Введите адрес в формате *улица дом квартира""")
                );
    }

    private CompletableFuture<State> two(Operation operation) {
        String[] address = operation.update().getMessage().getText().split("\s");
        return userRepository.findById(operation.state().getId())
                .thenCompose(user -> {
                    if (Objects.nonNull(user)) {
                        user.getTenant()
                                .setAddress(new Address()
                                        .setStreet(address[0])
                                        .setBuild(address[1])
                                        .setApartment(address[2]));
                        return userRepository.update(user)
                                .thenApply(updated -> {
                                    return operation.state()
                                            .setStep(Step.THREE)
                                            .setMessage("""
                                                    Введите информацию об автомобиле в следующем формате:
                                                    *модель цвет регистрационный номер*""");
                                });
                    }
                    log.error("two() -> not found user with ID {}", operation.state().getId());
                    return CompletableFuture.completedFuture(
                            operation.state()
                                    .setMessage("""
                                            Не найден пользователь с ID %d""".formatted(operation.state().getId())));
                });
    }
}
