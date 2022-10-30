package com.malerx.bot.handlers.state.nsm;

import com.malerx.bot.data.model.OutgoingMessage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface State {
    CompletableFuture<Optional<OutgoingMessage>> nextStep();
}
