package com.malerx.bot.handlers.state.nsm.register.user;

import com.malerx.bot.data.entity.Address;
import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.model.TextMessage;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.state.nsm.State;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SecondStepRegister implements State {
    private final Message message;
    private final PersistState state;

    private final StateRepository stateRepository;
    private final TGUserRepository userRepository;

    public SecondStepRegister(Update update,
                              PersistState state,
                              StateRepository stateRepository,
                              TGUserRepository userRepository) {
        this.message = update.getMessage();
        this.state = state;
        this.stateRepository = stateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CompletableFuture<Optional<OutgoingMessage>> next() {
        return userRepository.findById(message.getChatId())
                .thenCompose(user -> {
                    if (Objects.isNull(user))
                        return alreadyRegistered();

                    var address = createAddress();
                    if (address.isPresent())
                        return updateUser(user, address.get());

                    log.error("two() -> fail create address");
                    return CompletableFuture.completedFuture(Optional.of(
                            createMessage("""
                                    Ошибка при создании адреса. Проверьте введённые данные"""
                            )));
                });
    }

    private CompletableFuture<Optional<OutgoingMessage>> alreadyRegistered() {
        state.setStage(Stage.ERROR)
                .setDescription("Не найдет пользователь");
        var msg = createMessage("""
                Не найден пользователь с ID %d"""
                .formatted(message.getChatId()));
        return stateRepository.update(state)
                .thenApply(r -> Optional.of(msg));
    }

    private CompletableFuture<Optional<OutgoingMessage>> updateUser(TGUser user, Address address) {
        log.debug("updateTgUser() -> update user {}", user.getId());
        user.getTenant().setAddress(address);
        return userRepository.update(user)
                .thenCompose(updated -> {
                    state.setStage(Stage.DONE);
                    var msg = createMessage("""
                            Спасибо за регистрацию, теперь вам доступны \
                            дополнительные опции бота""");
                    return stateRepository.update(state)
                            .thenApply(r -> Optional.of(msg));
                });
    }

    private Optional<Address> createAddress() {
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

    private TextMessage createMessage(String text) {
        return new TextMessage(Set.of(message.getChatId()), text);
    }
}
