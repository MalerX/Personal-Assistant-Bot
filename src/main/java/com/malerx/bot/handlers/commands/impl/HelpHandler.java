package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.model.TextMessage;
import com.malerx.bot.handlers.commands.CommandHandler;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
            var m = new TextMessage(
                    Set.of(update.getMessage().getChatId()),
                    """
                            *register* - Регистрация пользователя в системе. Требуются *имя фамилия* и *адрес*. \
                            В БД заводится сущность TGUser.\040
                            *reg_car* - создаётся сущность Car и связывается с сущностью Tenet\040
                            *info* - Информация в системе
                            *help* - Помощь по командам бота"""
            );
            return Optional.of(m);
        });
    }

    @Override
    public Boolean support(@NonNull Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
