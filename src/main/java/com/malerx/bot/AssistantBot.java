package com.malerx.bot;

import com.malerx.bot.factory.BeanFactory;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
public class AssistantBot extends TelegramLongPollingBot {
    private final String token;
    private final String username;

    private final ArrayBlockingQueue<Update> requests;
    private final ArrayBlockingQueue<Object> responses;

    public AssistantBot(BeanFactory beanFactory,
                        @Value(value = "${telegram.token}") String token,
                        @Value(value = "${telegram.username}") String username
    ) {
        this.requests = beanFactory.getRequests();
        this.responses = beanFactory.getResponses();
        this.token = token;
        this.username = username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            requests.put(update);
        } catch (InterruptedException e) {
            log.info("onUpdateReceived() -> adding interrupted", e);
        }
    }

    @PostConstruct
    private void send() {
        CompletableFuture.runAsync(() -> {
            log.debug("send() -> start polling queue...");
            while (true) {
                try {
                    Object object = responses.take();
                    if (object instanceof SendMessage message) {
                        log.debug("send() -> object is SendMessage for {} with text {}",
                                message.getChatId(), message.getText());
                        execute(message);
                    } else {
                        if (object instanceof SendDocument document) {
                            log.debug("send() -> object is SenDocument for {}", document.getChatId());
                            execute(document);
                        }
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

