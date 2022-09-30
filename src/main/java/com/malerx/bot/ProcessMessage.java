package com.malerx.bot;

import com.malerx.bot.factory.BeanFactory;
import com.malerx.bot.handlers.HandlerManager;
import com.malerx.bot.statemachines.StateMachineManager;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;

@Context
@Slf4j
public class ProcessMessage {
    private static final String COMMAND_PREFIX = "/";
    private final ArrayBlockingQueue<Update> requests;
    private final ArrayBlockingQueue<Object> responses;
    private final HandlerManager handlerManager;
    private final StateMachineManager stateMachineManager;

    public ProcessMessage(BeanFactory factory,
                          HandlerManager handlerManager,
                          StateMachineManager stateMachineManager) {
        this.requests = factory.getRequests();
        this.responses = factory.getResponses();
        this.handlerManager = handlerManager;
        this.stateMachineManager = stateMachineManager;
    }

    @PostConstruct
    private void processing() {
        log.info("processing() -> start processing commandHandling incoming update...");
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    Update update = requests.take();
                    log.debug("processing() -> incoming update: {}", update.getMessage());
                    processingMessage(update)
                            .thenAcceptAsync(response -> {
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
                    throw new RuntimeException(e);
                }
            }
        });
    }

    CompletableFuture<Optional<Object>> processingMessage(@NonNull final Update update) {
        log.debug("processingMessage() -> processing incoming message from {}", update.getMessage().getChatId());
        if (update.getMessage().getText().startsWith(COMMAND_PREFIX)) {
            return handlerManager.commandHandling(update);
        } else {
//            Если текст входящего сообщения не начинается с "/", считаем что это продолжение ранее начатой операции конечного автомата
            return stateMachineManager.stateHandling(update);
        }
    }
}
