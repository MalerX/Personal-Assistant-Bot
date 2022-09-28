package com.malerx.bot.services.weather;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
class Coordinates {
    private final String latitude;
    private final String longitude;

    public Coordinates(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
