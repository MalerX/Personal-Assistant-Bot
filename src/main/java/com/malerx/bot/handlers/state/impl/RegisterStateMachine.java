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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.inject.Singleton;
import java.util.Optional;
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
        switch (operation.state().getStep()) {
            case ONE -> {
                return one(operation);
            }
            case TWO -> {
                return two(operation);
            }
        }
        var state = operation.state();
        log.error("proceed() -> wrong step '{}' for '{}'", state.getStep(), state.getMessage());
        return CompletableFuture.completedFuture(
                state.setStage(Stage.ERROR).setMessage("""
                        В данном процессе отсутствует настоящий запрошенный этап"""
                ));
    }

    private CompletableFuture<State> one(Operation operation) {
        var user = createUser(operation);
        return userRepository.save(user)
                .thenApply(u -> operation.state()
                        .setStep(Step.TWO)
                        .setMessage(
                                new SendMessage(
                                        operation.update().getMessage().getChatId().toString(),
                                        """
                                                Введите адрес в следующем формате формате
                                                *УЛИЦА ДОМ КВАРТИРА*"""))
                );
    }

    private TGUser createUser(Operation op) {
        var update = op.update();
        var message = update.getMessage();
//        var nick = message.getContact().getFirstName() + " " + message.getContact().getLastName();
        log.debug("createUser() -> contact: {}", message.getContact());
        var nick = "default";
        Tenant tenant = createTenant(message).orElse(new Tenant());
        log.debug("createUser() -> create tg user {} from message {}", message.getChatId(), message.getText());
        return new TGUser()
                .setId(message.getChatId())
                .setTenant(tenant)
                .setNickname(nick)
                .setRole(Role.TENANT);
    }

    private Optional<Tenant> createTenant(Message message) {
        var nameSurname = message.getText().split("\s");
        if (nameSurname.length == 2) {
            return Optional.of(new Tenant()
                    .setName(nameSurname[0])
                    .setSurname(nameSurname[1]));
        } else {
            log.error("createTenant() -> input format name/surname is not valid");
            return Optional.empty();
        }
    }

    private CompletableFuture<State> two(Operation operation) {
        var message = operation.update().getMessage();
        var address = createAddress(message).orElse(new Address());
        return userRepository.findById(operation.state().getChatId())
                .thenCompose(user -> {
                    log.debug("two() -> add Address to Tenant in user {}", user.getId());
                    user.getTenant()
                            .setAddress(address);
                    return userRepository.update(user)
                            .thenApply(updated -> operation.state()
                                    .setMessage(
                                            new SendMessage(
                                                    message.getChatId().toString(),
                                                    """
                                                            Спасибо за регистрацию, теперь вам доступны дополнительные опции бота"""))
                                    .setStage(Stage.DONE));
                });
    }

    private Optional<Address> createAddress(Message message) {
        log.debug("createAddress() -> create Address from {}", message.getText());
        var streetBuildNumber = message.getText().split("\s");
        if (streetBuildNumber.length == 3) {
            return Optional.of(new Address()
                    .setStreet(streetBuildNumber[0])
                    .setBuild(streetBuildNumber[1])
                    .setApartment(streetBuildNumber[2]));
        }
        {
            log.debug("createAddress() -> wrong input text: {}", message.getText());
            return Optional.empty();
        }
    }
}
