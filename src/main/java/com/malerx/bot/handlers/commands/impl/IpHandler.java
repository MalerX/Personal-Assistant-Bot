package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.enums.Role;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.model.TextMessage;
import com.malerx.bot.data.repository.TGUserRepository;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class IpHandler implements CommandHandler {
    private static final String COMMAND = "/ip";
    private final HttpClient client;
    private final String urlIp;
    private final TGUserRepository userRepository;

    public IpHandler(HttpClient client,
                     @Value("${api.yandex.ip}") String urlIp,
                     TGUserRepository userRepository) {
        this.client = client;
        this.urlIp = urlIp;
        this.userRepository = userRepository;
    }

    @Override
    public CompletableFuture<Optional<OutgoingMessage>> handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var chatId = update.getMessage().getChatId();
            return isAuthorized(chatId)
                    .thenCompose(authorized -> {
                        if (authorized)
                            return requestIp(chatId);
                        log.warn("handle() -> not authorized user requested host IP");
                        return CompletableFuture.completedFuture(
                                createMsg(chatId, "Запрос не авторизован"));
                    });
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private CompletableFuture<Boolean> isAuthorized(Long chatId) {
        log.debug("isAuthorized() -> check role user {}", chatId);
        return userRepository.existsById(chatId)
                .thenCompose(exist -> {
                    if (exist) {
                        return userRepository.findById(chatId)
                                .thenApply(u -> {
                                    if (Objects.equals(u.getRole(), Role.ADMIN))
                                        return Boolean.TRUE;
                                    log.warn("isAuthorized() -> user {} is not admin", u.getId());
                                    return Boolean.FALSE;
                                });
                    } else
                        log.debug("isAuthorized() -> user {} is not registered", chatId);
                    return CompletableFuture.completedFuture(Boolean.FALSE);
                });
    }

    private CompletableFuture<Optional<OutgoingMessage>> requestIp(Long chatId) {
        log.debug("requestIp() -> request hot ip");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(urlIp))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    if (StringUtils.isNotEmpty(r.body())) {
                        var ip = r.body().substring(1, r.body().length() - 1);
                        log.debug("requestIp() -> receive response with IP: {}", ip);
                        return createMsg(chatId, ip);
                    } else
                        return createMsg(chatId, "Ответ на запрос IP не получен");
                });
    }

    private Optional<OutgoingMessage> createMsg(Long chatId, String txt) {
        return Optional.of(new TextMessage(Set.of(chatId), txt));
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
