package com.malerx.bot.factory;

import io.micronaut.context.annotation.Factory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ArrayBlockingQueue;

@Factory
public class QueueFactory {
    private final ArrayBlockingQueue<Object> response;
    private final ArrayBlockingQueue<Update> request;

    public QueueFactory() {
        this.request = new ArrayBlockingQueue<>(1000);
        this.response = new ArrayBlockingQueue<>(1000);
    }

    public ArrayBlockingQueue<Object> getResponse() {
        return response;
    }

    public ArrayBlockingQueue<Update> getRequest() {
        return request;
    }
}
