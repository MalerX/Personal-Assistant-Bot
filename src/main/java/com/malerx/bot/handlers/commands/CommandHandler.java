package com.malerx.bot.handlers.commands;

import io.micronaut.core.annotation.NonNull;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис обработки входящих событий.
 */
public interface CommandHandler {
    /**
     * Обработка входящего события.
     *
     * @param update Объект события.
     * @return Ответ, созданный по результатам обработки события
     */
    CompletableFuture<Optional<Object>> handle(@NonNull Update update);

    Boolean support(@NonNull Update update);
}
