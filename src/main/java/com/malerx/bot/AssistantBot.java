package com.malerx.bot;

import com.malerx.bot.factory.QueueFactory;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;

@Context
@Slf4j
public class AssistantBot extends TelegramLongPollingBot {
    @Value(value = "${telegram.token}")
    private String token;
    @Value(value = "${telegram.username}")
    private String username;

    private final ArrayBlockingQueue<Update> requests;
    private final ArrayBlockingQueue<Object> responses;

    public AssistantBot(QueueFactory queueFactory) {
        this.requests = queueFactory.getRequests();
        this.responses = queueFactory.getResponses();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.debug("onUpdateReceived() -> receive requests from: {}", update.getMessage().getFrom());
            try {
                requests.put(update);
            } catch (InterruptedException e) {
                log.info("onUpdateReceived() -> adding interrupted", e);
            }
        } else {
            log.debug("onUpdateReceived() -> object Update not contain message or text");
        }
    }

    @PostConstruct
    private void send() {
        CompletableFuture.runAsync(() -> {
            log.debug("send() -> start polling queue...");
            while (true) {
                try {
                    Object object = responses.take();
                    log.debug("send() -> get responses {}", object);
                    if (object instanceof SendMessage) {
                        SendMessage message = ((SendMessage) object);
                        execute(message);
                    }
                } catch (Exception e) {
                    log.error("send() -> got error while polling queue:", e);
                }
            }
        });
    }

    @Override
    public String getBotToken() {
        return this.token;
    }

    @Override
    public String getBotUsername() {
        return this.username;
    }
}

