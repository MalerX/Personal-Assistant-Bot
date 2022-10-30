package com.malerx.bot.data.model;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Базовый класс сообщения ответа.
 */
abstract public class OutgoingMessage {
    private final UUID uuid = UUID.randomUUID();
    protected final Set<Long> destination;

    public OutgoingMessage(Set<Long> destination) {
        this.destination = destination;
    }

    abstract public Collection<Object> send();
}
