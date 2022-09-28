package com.malerx.bot.services.weather;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class WeatherService {
    @Value(value = "${api.yandex.weather}")
    private String weatherToken;
    @Value(value = "${api.yandex.urlWeather}")
    private String urlWeather;
    @Value(value = "${api.yandex.coordinates}")
    private String coordinatesToken;
    @Value(value = "${api.yandex.urlCoordinates}")
    private String urlCoordinates;
    private final HttpClient httpClient;

    public WeatherService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<Optional<Object>> getWeather(@NonNull Update update) {
        log.debug("handle() -> incoming request weather");
        String[] destination = update.getMessage().getText().split("\\s", 2);
        return getCoordinates(destination[1])
                .thenCompose(coordinates -> {
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
        return CompletableFuture.completedFuture(Coordinates.builder()
                .latitude("55.16")
                .longitude("12.11")
                .build());
    }

    @Getter
    @Builder
    private static class Coordinates {
        private final String latitude;
        private final String longitude;

        public Coordinates(String latitude, String longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

}
