package com.malerx.bot.handlers.state.nsm;

import com.malerx.bot.data.entity.Address;
import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SecondStepRegister implements State {
    private final User user;
    private final Message message;
    private final PersistState state;

    private final StateRepository stateRepository;
    private final TGUserRepository userRepository;

    public SecondStepRegister(Update update,
                              PersistState state,
                              StateRepository stateRepository,
                              TGUserRepository userRepository) {
        this.user = update.getMessage().getFrom();
        this.message = update.getMessage();
        this.state = state;
        this.stateRepository = stateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CompletableFuture<Optional<Object>> nextStep() {
        return userRepository.findById(message.getChatId())
                .thenCompose(tgUser -> {
                    if (Objects.isNull(tgUser)) {
                        state.setStage(Stage.ERROR)
                                .setDescription("Не найдет пользователь");
                        var msg = createMessage("""
                                Не найден пользователь с ID %d"""
                                .formatted(message.getChatId()));
                        return stateRepository.update(state)
                                .thenApply(r -> Optional.of(msg));
                    }
                    var address = createAddress();
                    address.ifPresentOrElse(a -> {
                        log.debug("two() -> add Address to Tenant in user {}", user.getId());
                        tgUser.getTenant().setAddress(a);
                    }, () -> log.error("two() -> address not create"));
                    if (address.isPresent()) {
                        return userRepository.update(tgUser)
                                .thenCompose(updated -> {
                                    state.setStage(Stage.DONE);
                                    var msg = createMessage("""
                                            Спасибо за регистрацию, теперь вам доступны \
                                            дополнительные опции бота""");
                                    return stateRepository.update(state)
                                            .thenApply(r -> Optional.of(msg));
                                });
                    }
                    log.error("two() -> fail create address");
                    return CompletableFuture.completedFuture(Optional.of(
                            createMessage("""
                                    Ошибка при создании адреса. Проверьте введённые данные"""
                            )));
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

    private SendMessage createMessage(String text) {
        var outgoing = new SendMessage(message.getChatId().toString(), text);
        outgoing.enableMarkdown(Boolean.TRUE);
        return outgoing;
    }
}
