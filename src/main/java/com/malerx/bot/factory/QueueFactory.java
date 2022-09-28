package com.malerx.bot.factory;

import io.micronaut.context.annotation.Factory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ArrayBlockingQueue;

@Factory
public class QueueFactory {
    private final ArrayBlockingQueue<Object> responses;
    private final ArrayBlockingQueue<Update> requests;

    public QueueFactory() {
        this.requests = new ArrayBlockingQueue<>(1000);
        this.responses = new ArrayBlockingQueue<>(1000);
    }

    public ArrayBlockingQueue<Object> getResponses() {
        return responses;
    }

    public ArrayBlockingQueue<Update> getRequests() {
        return requests;
    }
}
