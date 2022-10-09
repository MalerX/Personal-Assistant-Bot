package com.malerx.bot.handlers.commands.impl;

import com.malerx.bot.handlers.commands.CommandHandler;
import com.malerx.bot.services.weather.WeatherService;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class WeatherHandler implements CommandHandler {
    private static final String COMMAND = "/погода";

    private final WeatherService weatherService;

    public WeatherHandler(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public CompletableFuture<Optional<Object>> handle(@NonNull Update update) {
        return weatherService.getWeather(update);
    }

    @Override
    public Boolean support(@NonNull Update update) {
        return update.getMessage().getText().startsWith(COMMAND);
    }
}
