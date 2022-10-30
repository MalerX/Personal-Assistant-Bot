package com.malerx.bot.data.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ButtonMessage extends TextMessage {
    private final ReplyKeyboard keyboard;

    public ButtonMessage(String content,
                         Set<Long> destination,
                         ReplyKeyboard keyboard) {
        super(destination, content);
        this.keyboard = keyboard;
    }

    @Override
    public Collection<Object> send() {
        return super.send().stream()
                .peek(m -> {
                    if (m instanceof SendMessage sm)
                        sm.setReplyMarkup(keyboard);
                }).collect(Collectors.toSet());
    }
}
