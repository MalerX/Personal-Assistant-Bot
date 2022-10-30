package com.malerx.bot.handlers.state.nsm.register.user;

import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.entity.Tenant;
import com.malerx.bot.data.enums.Role;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.model.TextMessage;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.state.nsm.State;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class FirstStepRegister implements State {
    private final User user;
    private final Message message;
    private final PersistState state;

    private final StateRepository stateRepository;
    private final TGUserRepository userRepository;

    public FirstStepRegister(Update update,
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
    public CompletableFuture<Optional<OutgoingMessage>> nextStep() {
        var tgUser = createUser();
        var tenant = createTenant();
        if (tenant.isEmpty()) {
            log.error("one() -> fail create tenant {}", message);
            return CompletableFuture.completedFuture(
                    Optional.of(createMessage("""
                            Ошибка выполнения действия. Проверьте введённые данные""")));
        } else
            tgUser.setTenant(tenant.get());
        log.debug("one() -> prepare user {}", tgUser);
        return userRepository.save(tgUser)
                .thenCompose(u -> {
                    state.setStep(Step.TWO);
                    return stateRepository.update(state)
                            .thenApply(r -> Optional.of(
                                    createMessage("""
                                            Введите адрес в следующем формате формате\
                                            (улица/дом/квартира на отдельных строках):
                                                           
                                            \t*УЛИЦА
                                            \tДОМ
                                            \tКВАРТИРА*""")
                            ));
                });
    }

    private TGUser createUser() {
        log.debug("createUser() -> contact: {}", message.getContact());
        var nick = getNickname();
        log.debug("createUser() -> create tg user {} from message {}", message.getChatId(), message.getText());
        return new TGUser()
                .setId(message.getChatId())
                .setNickname(nick)
                .setRole(Role.TENANT);
    }

    private String getNickname() {
        log.debug("getNickname() -> get nickname user {}", message.getChatId());
        var firstName = user.getFirstName();
        var lastName = user.getLastName();
        log.debug("getNickname() -> create nickname '{} {}'", firstName, lastName);
        return firstName + " " + lastName;
    }

    private Optional<Tenant> createTenant() {
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

    private TextMessage createMessage(String text) {
        return new TextMessage(Set.of(message.getChatId()), text);
    }
}
