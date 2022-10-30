package com.malerx.bot.data.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ButtonMessage extends TextMessage {
    private final ReplyKeyboard keyboard;

    public ButtonMessage(String content,
                         Set<Long> destination,
                         ReplyKeyboard keyboard) {
        super(destination, content);
        this.keyboard = keyboard;
    }

    @Override
    public Stream<Object> send() {
        return super.send()
                .peek(m -> {
                    if (m instanceof SendMessage sm)
                        sm.setReplyMarkup(keyboard);
                });
    }
}
