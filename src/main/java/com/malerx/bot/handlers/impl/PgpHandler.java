package com.malerx.bot.handlers.impl;

import com.malerx.bot.data.entity.GpgRecord;
import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.enums.Role;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.CommandHandler;
import io.micronaut.core.util.CollectionUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
    public CompletableFuture<Optional<Object>> handle(Update update) {
        if (update.hasMessage()) {
            log.debug("handle() -> incoming request owner token from {}", update.getMessage().getChatId());
            return tgUserRepository.findByRole(Role.ADMIN)
                    .thenApply(admin -> {
                        var message = admin.stream()
                                .map(TGUser::getGpgPublicKeys)
                                .reduce((identity, accumulator) -> {
                                    accumulator.addAll(identity);
                                    return accumulator;
                                });
                        if (message.isPresent()) {
                            return Optional.of(new SendDocument(
                                    update.getMessage().getChatId().toString(),
                                    createInputFile(buildString(message.get()))));
                        }
                        return Optional.of(new SendMessage(
                                update.getMessage().getChatId().toString(),
                                """
                                        В локальной базе данных не найдены записи PGP ключей \
                                        администраторов системы.
                                        """));
                    });

        }
        log.warn("handle() -> incoming request not contain message");
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private String buildString(Set<GpgRecord> records) {
        StringBuffer sb = new StringBuffer();
        if (CollectionUtils.isNotEmpty(records))
            records.forEach(record -> sb.append(record.getPgpKey()).append("\n\n"));
        return sb.toString();
    }

    private InputFile createInputFile(String keys) {
        InputFile file = null;
        try (InputStream is = new ByteArrayInputStream(keys.getBytes())) {
            file = new InputFile(is, "key.txt");
        } catch (Exception e) {
            log.error("handle() -> fail create keys.txt");
        }
        return file;
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
