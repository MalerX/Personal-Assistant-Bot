package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.data.enums.Role;
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
    public CompletableFuture<Optional<Object>> handle(Update update) {
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
                .thenApply(exist -> {
                    if (exist) {
                        return userRepository.findById(chatId)
                                .thenApply(u -> Objects.equals(u.getRole(), Role.ADMIN)).join();
                    } else
                        log.debug("isAuthorized() -> user {} is not registered", chatId);
                    return Boolean.FALSE;
                });
    }

    private CompletableFuture<Optional<Object>> requestIp(Long chatId) {
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

    private Optional<Object> createMsg(Long chatId, String txt) {
        return Optional.of(new SendMessage(chatId.toString(), txt));
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
