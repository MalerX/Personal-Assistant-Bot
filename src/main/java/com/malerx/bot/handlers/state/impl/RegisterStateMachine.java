package com.malerx.bot.handlers.state.impl;

import com.malerx.bot.data.entity.Address;
import com.malerx.bot.data.entity.State;
import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.entity.Tenant;
import com.malerx.bot.data.enums.Role;
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

import javax.inject.Singleton;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class RegisterStateMachine implements StateHandler {
    private final TGUserRepository userRepository;
    private final StateRepository stateRepository;

    public RegisterStateMachine(TGUserRepository userRepository,
                                StateRepository stateRepository) {
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
    }

    @Override
    @NonNull
    public CompletableFuture<Optional<Object>> proceed(@NonNull Operation operation) {
        switch (operation.state().getStep()) {
            case ONE -> {
                return one(operation);
            }
            case TWO -> {
                return two(operation);
            }
        }
        var s = operation.state()
                .setStage(Stage.ERROR)
                .setMessage("""
                        В данном процессе отсутствует запрошенный этап""");
        log.error("proceed() -> wrong step '{}' for '{}'", s.getStep(), s.getMessage());
        return updateState(s)
                .thenApply(r -> Optional.of(s.toMessage()));
    }

    private CompletableFuture<Optional<Object>> one(Operation operation) {
        var message = operation.update().getMessage();
        var user = createUser(message);
        var tenant = createTenant(message);
        if (tenant.isEmpty()) {
            log.error("one() -> fail create tenant {}", message);
            return CompletableFuture.completedFuture(Optional.of(
                    createMessage(message.getChatId(),
                            """
                                    Ошибка выполнения действия. Проверьте введённые данные""")));
        } else
            user.setTenant(tenant.get());
        log.debug("one() -> prepare user {}", user);
        return userRepository.save(user)
                .thenCompose(u -> {
                    var s = operation.state()
                            .setStep(Step.TWO)
                            .setMessage(
                                    """
                                            Введите адрес в следующем формате формате\
                                            (улица/дом/квартира на отдельных строках):
                                                           
                                            \t*УЛИЦА
                                            \tДОМ
                                            \tКВАРТИРА*""");

                    return updateState(s)
                            .thenApply(r -> Optional.of(s.toMessage()));
                });
    }

    private TGUser createUser(final Message message) {
        log.debug("createUser() -> contact: {}", message.getContact());
        var nick = getNickname(message);
        log.debug("createUser() -> create tg user {} from message {}", message.getChatId(), message.getText());
        return new TGUser()
                .setId(message.getChatId())
                .setNickname(nick)
                .setRole(Role.TENANT);
    }

    private String getNickname(final Message m) {
        log.debug("getNickname() -> get nickname user {}", m.getChatId());
        var firstName = m.getFrom().getFirstName();
        var lastName = m.getFrom().getLastName();
        log.debug("getNickname() -> create nickname '{} {}'", firstName, lastName);
        return firstName + " " + lastName;
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

    private CompletableFuture<Optional<Object>> two(Operation operation) {
        var message = operation.update().getMessage();
        return userRepository.findById(message.getChatId())
                .thenCompose(user -> {
                    if (Objects.isNull(user)) {
                        var s = operation.state()
                                .setStage(Stage.ERROR)
                                .setMessage("""
                                        Не найден пользователь с ID %d"""
                                        .formatted(message.getChatId()));
                        return updateState(s)
                                .thenApply(r -> Optional.of(s.toMessage()));
                    }
                    var address = createAddress(message);
                    address.ifPresentOrElse(a -> {
                        log.debug("two() -> add Address to Tenant in user {}", user.getId());
                        user.getTenant().setAddress(a);
                    }, () -> log.error("two() -> address not create"));
                    if (address.isPresent()) {
                        return userRepository.update(user).thenCompose(updated -> {
                            var s = operation.state()
                                    .setMessage(createMessage(message.getChatId(),
                                            """
                                                    Спасибо за регистрацию, теперь вам доступны \
                                                    дополнительные опции бота"""))
                                    .setStage(Stage.DONE);
                            return updateState(s)
                                    .thenApply(r -> Optional.of(s.toMessage()));
                        });
                    }
                    log.error("two() -> fail create address");
                    return CompletableFuture.completedFuture(Optional.of(
                            createMessage(message.getChatId(),
                                    """
                                            Ошибка при создании адреса. Проверьте введённые данные""")));
                });
    }

    private Optional<Address> createAddress(Message message) {
        log.debug("createAddress() -> create Address from {}", message.getText());
        var streetBuildNumber = message.getText().split("\n");
        if (streetBuildNumber.length == 3) {
            return Optional.of(new Address()
                    .setStreet(streetBuildNumber[0])
                    .setBuild(streetBuildNumber[1])
                    .setApartment(streetBuildNumber[2]));
        }
        {
            log.error("createAddress() -> wrong input text: {}", message.getText());
            return Optional.empty();
        }
    }

    private SendMessage createMessage(Long id, String text) {
        var message = new SendMessage(id.toString(), text);
        message.enableMarkdown(Boolean.TRUE);
        return message;
    }

    CompletableFuture<State> updateState(State s) {
        log.debug("updateState() -> update State {}", s);
        return stateRepository.update(s);
    }
}
