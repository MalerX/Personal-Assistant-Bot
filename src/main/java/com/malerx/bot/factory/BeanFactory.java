package com.malerx.bot.factory;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.net.http.HttpClient;
import java.util.concurrent.ArrayBlockingQueue;

@Factory
public class BeanFactory {
    private final ArrayBlockingQueue<Object> responses;
    private final ArrayBlockingQueue<Update> requests;

    public BeanFactory() {
        this.requests = new ArrayBlockingQueue<>(1000);
        this.responses = new ArrayBlockingQueue<>(1000);
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    public ArrayBlockingQueue<Object> getResponses() {
        return responses;
    }

    public ArrayBlockingQueue<Update> getRequests() {
        return requests;
    }
}
