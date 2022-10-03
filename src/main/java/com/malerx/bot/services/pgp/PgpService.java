package com.malerx.bot.services.pgp;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class PgpService {
    @Value(value = "${gpg.public.key}")
    private String path;

    public CompletableFuture<Optional<Object>> getPgpKey(@NonNull final Update update) {
        if (Objects.nonNull(path)) {
            return CompletableFuture.supplyAsync(() -> {
                log.debug("getPgpKey() -> upload signed message with public key");
                try {
                    return Optional.of(
                            new SendMessage(update.getMessage().getChatId().toString(),
                                    new String(Files.readAllBytes(Path.of(path)))));
                } catch (IOException e) {
                    log.error("getPgpKey() -> failed read file {}", path, e);
                }
                return Optional.empty();
            });
        }
        log.error("getPgpKey() -> not found path to public key");
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
