package com.malerx.bot.services.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import lombok.Builder;
import lombok.Getter;
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
    @Value(value = "${api.yandex.urlWeather}")
    private String urlWeather;
    @Value(value = "${api.yandex.geo}")
    private String geoToken;
    @Value(value = "${api.yandex.urlGeo}")
    private String urlGeo;
    private final HttpClient httpClient;
    private Position position;

    public WeatherService(HttpClient httpClient, Position position) {
        this.httpClient = httpClient;
        this.position = position;
    }

    public CompletableFuture<Optional<Object>> getWeather(@NonNull Update update) {
        log.debug("handle() -> incoming request weather");
        String[] destination = update.getMessage().getText().split("\\s", 2);
        return getCoordinates(destination[1])
                .thenCompose(coordinates -> {
                    if (Objects.isNull(coordinates)) {
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                    String geo = String.format("?lat=%s&lon=%s",
                            coordinates.getLatitude(), coordinates.getLongitude());
                    HttpRequest request = HttpRequest.newBuilder()
                            .GET()
                            .uri(URI.create(urlWeather.concat(geo)))
                            .header("X-Yandex-API-Key", weatherToken)
                            .build();
                    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenApply(httpResponse -> {
                                log.debug("handle() -> receive response from {}: {}",
                                        request.uri(), httpResponse.body());
                                SendMessage message = new SendMessage(
                                        update.getMessage().getChatId().toString(),
                                        httpResponse.body()
                                );
                                return Optional.of(message);
                            });
                });
    }

    private CompletableFuture<Coordinates> getCoordinates(String destination) {
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
                        Optional<Coordinates> geo = position.extract(httpResponse.body());
                        if (geo.isPresent()) {
                            return geo.get();
                        }
                        log.error("getCoordinates() -> failed get position for {}", destination);
                    } else {
                        log.error("getCoordinates() -> response body is empty");
                    }
                    return null;
                });
    }

    @Getter
    @Builder
    public static class Coordinates {
        private final String latitude;
        private final String longitude;

        public Coordinates(String latitude, String longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}