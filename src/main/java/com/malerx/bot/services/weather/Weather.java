package com.malerx.bot.services.weather;

import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@Slf4j
public class Weather {

    public String getAnswer(@NonNull String jsonWeather) {
        log.debug("getAnswer() -> in development");
        return "";
    }
}
