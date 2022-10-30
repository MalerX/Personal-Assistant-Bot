package com.malerx.bot.data.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class TextMessage extends OutgoingMessage {
    private final String content;

    public TextMessage(Set<Long> destination,
                       String content) {
        super(destination);
        this.content = content;
    }

    @Override
    public Collection<Object> send() {
        return destination.stream()
                .map(id -> {
                    var msg = new SendMessage(id.toString(), content);
                    msg.enableMarkdown(Boolean.TRUE);
                    return msg;
                })
                .collect(Collectors.toSet());
    }
}
