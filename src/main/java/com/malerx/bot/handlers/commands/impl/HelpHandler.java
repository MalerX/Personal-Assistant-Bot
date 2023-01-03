package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.model.ButtonMessage;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.handlers.commands.CommandHandler;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Slf4j
public class HelpHandler implements CommandHandler {
    private static final String COMMAND = "/help";

    @Override
    public CompletableFuture<Optional<OutgoingMessage>> handle(@NonNull Update update) {
        return CompletableFuture.supplyAsync(() -> {
            var message = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() :
                    (update.hasMessage() ? update.getMessage() : null);
//            var m = new TextMessage(
//                    Set.of(message.getChatId()),
//                    """
//                            *register* - Регистрация пользователя в системе. Требуются *имя фамилия* и *адрес*. \
//                            В БД заводится сущность TGUser.\040
//                            *reg_car* - создаётся сущность Car и связывается с сущностью Tenet\040
//                            *info* - Информация в системе
//                            *help* - Помощь по командам бота"""
//            );
            return Optional.of(buildHelpMessage(message.getChatId()));
        });
    }

    private OutgoingMessage buildHelpMessage(long chatId) {
        var welcome = "Добро пожаловать в мультимедийную интерактивную систему бота";

        var keyboard = InlineKeyboardMarkup.builder()
                .keyboard(createKeyboards())
                .build();
        return new ButtonMessage(welcome, Set.of(chatId), keyboard);
    }

    private List<? extends List<InlineKeyboardButton>> createKeyboards() {
        var register = InlineKeyboardButton.builder()
                .text("Регистарция в системе")
                .callbackData("/register")
                .build();
        var info = InlineKeyboardButton.builder()
                .text("Информация в системе")
                .callbackData("/info")
                .build();
        var help = InlineKeyboardButton.builder()
                .text("Помощь по командам бота")
                .callbackData("/help")
                .build();
        var carReg = InlineKeyboardButton.builder()
                .text("Регистрация автомобиля")
                .callbackData("/reg_car")
                .build();
        var pass = InlineKeyboardButton.builder()
                .text("Заказать пропуск на автомобиль")
                .callbackData("/pass")
                .build();
        return Stream.of(register, info, help, carReg, pass)
                .map(List::of)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean support(@NonNull Update update) {
        String flag = update.hasCallbackQuery() ? update.getCallbackQuery().getData() :
                (update.hasMessage() ? update.getMessage().getText() : "");
        return flag.startsWith(COMMAND);
    }
}
