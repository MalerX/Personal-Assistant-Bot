package com.malerx.bot;

import com.malerx.bot.factory.BeanFactory;
import com.malerx.bot.handlers.HandlerManager;
import io.micronaut.context.annotation.Context;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;

@Context
@Slf4j
public class ProcessMessage {
    private final ArrayBlockingQueue<Update> requests;
    private final ArrayBlockingQueue<Object> responses;
    private final HandlerManager handlerManager;

    public ProcessMessage(BeanFactory factory,
                          HandlerManager handlerManager) {
        this.requests = factory.getRequests();
        this.responses = factory.getResponses();
        this.handlerManager = handlerManager;
    }

    @PostConstruct
    private void processing() {
        log.info("processing() -> start processing handle incoming update...");
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    Update update = requests.take();
                    log.debug("processing() -> incoming update from: {}", update.getMessage().getChatId());
                    handlerManager.handle(update)
                            .thenAcceptAsync(response -> {
                                response.ifPresent(this::define);
                            });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void define(Object o) {
        if (o instanceof Collection<?> os)
            os.forEach(this::send);
        else
            send(o);
    }

    private void send(Object o) {
        try {
            responses.put(o);
        } catch (InterruptedException e) {
            log.error("processing() -> interrupt add response to queue");
        }
    }
}
