package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.model.TextMessage;
import com.malerx.bot.handlers.commands.CommandHandler;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class HelpHandler implements CommandHandler {
    private static final String COMMAND = "/help";

    @Override
    public CompletableFuture<Optional<OutgoingMessage>> handle(@NonNull Update update) {
        return CompletableFuture.supplyAsync(() -> {
            var message = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() :
                    (update.hasMessage() ? update.getMessage() : null);
            if (message != null) {
                return Optional.of(createMessage(message.getChatId()));
            }
            return Optional.empty();
        });
    }

    private OutgoingMessage createMessage(long chatId) {
        return new TextMessage(
                Set.of(chatId),
                """
                        Добро пожаловать в интерактивную мультимедийную систему бота Технопарка.
                                                    
                        */register* - регистрация пользователя в системе.\040
                        */reg_car* - зарегистрировать в системе автомобиль для безпрепятственного въезда на территорию.\040
                        */info* - Информация о пользователе в системе
                        */help* - Помощь по командам бота"""
        );
    }

    @Override
    public Boolean support(@NonNull Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
