package com.malerx.bot.data.model;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Базовый класс сообщения ответа.
 */
abstract public class OutgoingMessage {
    private final UUID uuid = UUID.randomUUID();
    protected final Set<Long> destination;

    public OutgoingMessage(Set<Long> destination) {
        this.destination = destination;
    }

    abstract public Stream<Object> send();
}
