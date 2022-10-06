package com.malerx.bot.services.weather;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
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
public class WeatherService {
    @Value(value = "${api.yandex.weather}")
    private String weatherToken;
    @Value(value = "${api.yandex.geo}")
    private String geoToken;
    @Value(value = "${api.yandex.urlGeo}")
    private String urlGeo;

    private final HttpClient httpClient;
    private final Position position;
    private final Weather weather;

    public WeatherService(HttpClient httpClient, Position position, Weather weather) {
        this.httpClient = httpClient;
        this.position = position;
        this.weather = weather;
    }

    public CompletableFuture<Optional<Object>> getWeather(@NonNull Update update) {
        log.debug("handle() -> incoming request weather");
        String[] destination = update.getMessage().getText().split("\\s", 2);
        return getCoordinates(destination[1])
                .thenCompose(coordinates -> {
                    if (coordinates.isPresent()) {
                        return getWeather(coordinates.get())
                                .thenApply(jsonOptional -> jsonOptional
                                        .map(jsonWeather -> new SendMessage(
                                                update.getMessage().getChatId().toString(), jsonWeather
                                        )));
                    }
                    log.error("getCoordinates() -> failed get position for {}", destination[1]);
                    return CompletableFuture.completedFuture(Optional.empty());
                });
    }

    private CompletableFuture<Optional<Coordinates>> getCoordinates(String destination) {
        log.debug("getCoordinates() -> send request pos for {}", destination);
        String uriStr = urlGeo.concat(
                String.format("?format=json&apikey=%s&geocode=%s",
                        geoToken, destination.replace(" ", "+")));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uriStr))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> {
                    log.debug("getCoordinates() -> receive response with pos");
                    if (StringUtils.isNotEmpty(httpResponse.body())) {
                        return position.extract(httpResponse.body());
                    } else {
                        log.error("getCoordinates() -> response body is empty");
                    }
                    return Optional.empty();
                });
    }

    private CompletableFuture<Optional<String>> getWeather(Coordinates coordinates) {
        if (Objects.isNull(coordinates)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(coordinates.getUri())
                .header("X-Yandex-API-Key", weatherToken)
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> Optional.ofNullable(httpResponse.body()));
    }
}
