package com.malerx.bot.services.weather;

import lombok.Builder;

import java.net.URI;

@Builder
class Coordinates {
    private static final String urlWeather = "https://api.weather.yandex.ru/v2/informers";
    private final String latitude;
    private final String longitude;

    public Coordinates(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public URI getUri() {
        return URI.create(
                urlWeather.concat(String.format("?lat=%s&lon=%s", latitude, longitude)));
    }
}
