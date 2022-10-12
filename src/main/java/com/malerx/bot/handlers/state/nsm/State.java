package com.malerx.bot.handlers.state.nsm;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface State {
    CompletableFuture<Optional<Object>> nextStep();
}
