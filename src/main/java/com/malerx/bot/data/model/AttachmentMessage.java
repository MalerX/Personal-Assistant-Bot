package com.malerx.bot.data.model;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AttachmentMessage extends OutgoingMessage {
    private final InputFile file;

    public AttachmentMessage(Set<Long> destination,
                             InputFile file) {
        super(destination);
        this.file = file;
    }

    @Override
    public Collection<Object> send() {
        return destination.stream()
                .map(id -> new SendDocument(id.toString(), file))
                .collect(Collectors.toSet());
    }
}
