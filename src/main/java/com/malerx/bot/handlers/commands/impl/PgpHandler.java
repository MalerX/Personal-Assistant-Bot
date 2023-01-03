package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.entity.GpgRecord;
import com.malerx.bot.data.enums.Role;
import com.malerx.bot.data.model.AttachmentMessage;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.model.TextMessage;
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
import java.util.Set;
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
    public CompletableFuture<Optional<OutgoingMessage>> handle(@NonNull Update update) {
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

    private OutgoingMessage createDocument(Update update, String keys) {
        return new AttachmentMessage(
                Set.of(update.getMessage().getChatId()),
                new InputFile(new ByteArrayInputStream(
                        keys.getBytes()),
                        "keys.txt"));
    }

    private Optional<OutgoingMessage> createMessage(Update update) {
        return Optional.of(new TextMessage(
                Set.of(update.getMessage().getChatId()),
                """
                        В локальной базе данных не найдены записи PGP ключей \
                        администраторов системы.
                        """
        ));
    }

    @Override
    public Boolean support(@NonNull Update update) {
        String flag = update.hasCallbackQuery() ? update.getCallbackQuery().getData() :
                (update.hasMessage() ? update.getMessage().getText() : "");
        return flag.startsWith(COMMAND);
    }
}
