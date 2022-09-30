package com.malerx.bot.statemachines.impl;

import com.malerx.bot.statemachines.StateMachine;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class RegisterUserMachine implements StateMachine {
    @Override
    public CompletableFuture<Optional<Object>> handle(Update update) {
        return null;
    }
}
