package com.malerx.bot.service.handlers;

import io.micronaut.core.annotation.NonNull;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис обработки входящих событий.
 */
public interface UpdateHandler {
    /**
     * Обработка входящего события.
     *
     * @param update Объект события.
     * @return Ответ, созданный по результатам обработки события
     */
    CompletableFuture<Optional<Object>> handle(@NotNull Update update);

    Boolean support(@NonNull Update update);
}
