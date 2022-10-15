package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.handlers.commands.CommandHandler;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class IpHandler implements CommandHandler {
    private static final String COMMAND = "/ip ";
    private final HttpClient client;
    private final String urlIp;

    public IpHandler(HttpClient client,
                     @Value("${api.ip}") String urlIp) {
        this.client = client;
        this.urlIp = urlIp;
    }

    @Override
    public CompletableFuture<Optional<Object>> handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.debug("handle() -> request host ip");
            var chatId = update.getMessage().getChatId();
            HttpRequest r = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(urlIp))
                    .build();
            return client.sendAsync(r, HttpResponse.BodyHandlers.ofString())
                    .thenApply(ip -> {
                        if (StringUtils.isNotEmpty(ip.body())) {
                            return createMsg(chatId, ip.body());
                        } else
                            return createMsg(chatId, "Ответ на запрос IP не получен");
                    });
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private Optional<Object> createMsg(Long chatId, String txt) {
        return Optional.of(new SendMessage(chatId.toString(), txt));
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
