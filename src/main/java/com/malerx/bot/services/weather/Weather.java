package com.malerx.bot.services.weather;

import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Singleton
@Slf4j
public class Weather {

    public String getAnswer(@NonNull String jsonWeather) {
        log.debug("getAnswer() -> in development");
        return "";
    }

    @Override
    public String toString() {
        return """
                Необходимо использовать преимущество последней версии ЯП.
                """;
    }
}
