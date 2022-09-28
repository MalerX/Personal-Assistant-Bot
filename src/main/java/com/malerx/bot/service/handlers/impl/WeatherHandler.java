package com.malerx.bot.service.handlers.impl;

import com.malerx.bot.service.handlers.UpdateHandler;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Value;
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
public class WeatherHandler implements UpdateHandler {
    private static final String COMMAND = "/погода ";
    @Value(value = "${api.yandex.weather}")
    private String weatherToken;
    private final HttpClient client;

    public WeatherHandler() {
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public CompletableFuture<Optional<Object>> handle(Update update) {
        log.debug("handle() -> incoming request weather");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://api.weather.yandex.ru/v2/informers?lat=55.16&lon=12.11"))
                .header("X-Yandex-API-Key", weatherToken)
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> {
                    log.debug("handle() -> receive response from {}: {}",
                            request.uri(), httpResponse.body());
                    SendMessage message = new SendMessage(
                            update.getMessage().getChatId().toString(),
                            httpResponse.body()
                    );
                    return Optional.of(message);
                });
    }

    @Override
    public Boolean support(Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
