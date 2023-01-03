package com.malerx.bot.handlers.state.nsm.pass;

import com.malerx.bot.data.model.CallbackData;
import com.malerx.bot.data.model.OutgoingMessage;
import com.malerx.bot.data.repository.CarRepository;
import com.malerx.bot.handlers.state.nsm.State;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class SecondStepGettingPassState implements State {
    private final CarRepository carRepository;
    private final CallbackData callbackData;
    private final Update update;


    @Override
    public CompletableFuture<Optional<OutgoingMessage>> next() {
        return null;
    }
}
