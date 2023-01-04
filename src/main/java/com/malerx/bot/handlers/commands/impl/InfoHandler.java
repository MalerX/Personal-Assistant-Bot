package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.model.ButtonMessage;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.commands.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.Set;
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
    public CompletableFuture<Optional<OutgoingMessage>> handle(Update update) {
        var chatId = update.getMessage().getChatId();
        if (chatId != null) {
            log.debug("handle() -> request info by user {}", chatId);
            return userRepository.existsById(chatId)
                    .thenApply(exist -> {
                        if (exist) {
                            return userRepository.findById(chatId)
                                    .thenApply(user -> {
                                        log.debug("handle() -> found user {}", user.getId());
                                        var info = prepareInfo(user);
                                        return createMsg(chatId, info);
                                    }).join();
                        }
                        return createMsg(chatId, """
                                Вы не прошли регистрацию в системе бота. Зарегистрируйтесь по команде
                                \t\t\t*/register*""");
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

    private Optional<OutgoingMessage> createMsg(long chatId, String s) {
        var k = ReplyKeyboardRemove.builder()
                .removeKeyboard(Boolean.TRUE)
                .build();
        return Optional.of(new ButtonMessage(s, Set.of(chatId), k));
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
