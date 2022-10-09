package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.commands.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class InfoHandler implements CommandHandler {
    private static final String COMMAND = "/info";
    private final TGUserRepository userRepository;

    public InfoHandler(TGUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CompletableFuture<Optional<Object>> handle(Update update) {
        log.debug("handle() -> request info by user {}", update.getMessage().getChatId());
        if (update.hasMessage() && update.getMessage().hasText()) {
            var chatId = update.getMessage().getChatId();
            return userRepository.existsById(chatId)
                    .thenCompose(exist -> {
                        if (exist) {
                            return userRepository.findById(chatId)
                                    .thenApply(user -> {
                                        log.debug("handle() -> found user {}", user.getId());
                                        var info = prepareInfo(user);
                                        return createMsg(update, info);
                                    });
                        }
                        return CompletableFuture.completedFuture(
                                createMsg(update, """
                                        Вы не прошли регистрацию в системе бота. Зарегистрируйтесь по команде
                                        \t\t\t*/register*"""));
                    });
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private String prepareInfo(TGUser u) {
        var t = u.getTenant();
        var a = t.getAddress();
        var c = t.getCars();
        var auto = c.isEmpty() ? "" : "Автомобили: " + c;
        return "Пользователь: " + t.getName() + " " + t.getSurname() + "\n" +
                "nickname: " + u.getNickname() + "\n" +
                "\nАдрес:\n" +
                "\t\tулица " + a.getStreet() + "\n" +
                "\t\tстроение " + a.getBuild() + "\n" +
                "\t\tапартаменты " + a.getApartment() + "\n\n" +
                auto;

    }

    private Optional<Object> createMsg(final Update u, String s) {
        var msg = new SendMessage(
                u.getMessage().getChatId().toString(),
                s
        );
        msg.enableMarkdown(Boolean.TRUE);
        return Optional.of(msg);
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
