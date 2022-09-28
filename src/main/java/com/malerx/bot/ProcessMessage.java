package com.malerx.bot;

import com.malerx.bot.factory.BeanFactory;
import com.malerx.bot.handlers.HandlerManager;
import io.micronaut.context.annotation.Context;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
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
                    log.debug("processing() -> incoming update: {}", update.getMessage());
                    handlerManager.handle(update)
                            .thenAccept(response -> {
                                if (response.isPresent()) {
                                    try {
                                        responses.put(response.get());
                                    } catch (InterruptedException e) {
                                        log.error("processing() -> failed add response to queue");
                                    }
                                } else {
                                    log.error("processing() -> response is empty;");
                                }
                            });
                } catch (InterruptedException e) {
                    log.error("processing() -> got error while polling queue...", e);
                }
            }
        });
    }

}
