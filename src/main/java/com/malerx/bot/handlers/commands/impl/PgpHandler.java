package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.entity.GpgRecord;
import com.malerx.bot.data.enums.Role;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.commands.CommandHandler;
import io.micronaut.core.annotation.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class PgpHandler implements CommandHandler {
    private static final String COMMAND = "/pgp";

    private final TGUserRepository tgUserRepository;

    public PgpHandler(TGUserRepository tgUserRepository) {
        this.tgUserRepository = tgUserRepository;
    }

    @SneakyThrows
    @Override
    public CompletableFuture<Optional<Object>> handle(@NonNull Update update) {
        if (update.hasMessage()) {
            log.debug("handle() -> incoming request owner token from {}", update.getMessage().getChatId());
            return tgUserRepository.findByRole(Role.ADMIN)
                    .thenApply(admins -> admins.stream()
                            .flatMap(user -> user.getGpgPublicKeys().stream())
                            .map(GpgRecord::getPgpKey)
                            .reduce((s1, s2) -> s2.concat("\n\n\n".concat(s1)))
                            .map(keys -> createDocument(update, keys))
                            .or(() -> createMessage(update)));
        }
        log.warn("handle() -> incoming request not contain message");
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private Object createDocument(Update update, String keys) {
        return new SendDocument(
                update.getMessage().getChatId().toString(),
                new InputFile(new ByteArrayInputStream(
                        keys.getBytes()),
                        "keys.txt"));
    }

    private Optional<Object> createMessage(Update update) {
        return Optional.of(new SendMessage(
                update.getMessage().getChatId().toString(),
                """
                        В локальной базе данных не найдены записи PGP ключей \
                        администраторов системы.
                        """
        ));
    }

    @Override
    public Boolean support(@NonNull Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
