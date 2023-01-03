package com.malerx.bot.data.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malerx.bot.data.entity.Car;
import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.handlers.state.nsm.State;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PassMessage extends OutgoingMessage {
    private final PersistState state;
    private final TGUser user;
    private final Long operatorId;
    private final ObjectMapper mapper;

    public PassMessage(Set<Long> destination, TGUser user, Long operatorId, PersistState state) {
        super(destination);
        this.user = user;
        this.operatorId = operatorId;
        this.mapper = new ObjectMapper();
        this.state = state;
    }

    @Override
    public Collection<Object> send() {
        Collection<Object> messages = new ArrayList<>(createApproveRequest());
        messages.addAll(createUsersMessages());
        return messages;
    }

    private Collection<SendMessage> createUsersMessages() {
        var msg = "Запрос на получение пропуска отправлен оператору. Вы будете уведомлены о принятом решения";
        return destination.stream()
                .map(id -> new SendMessage(id.toString(), msg))
                .collect(Collectors.toList());
    }

    private Collection<SendMessage> createApproveRequest() {
        return user.getTenant().getCars().stream()
                .filter(c -> !c.getActive())
                .map(this::createMsg)
                .collect(Collectors.toList());
    }

    private SendMessage createMsg(final Car car) {
        var tenant = user.getTenant();
        var carId = car.getId();
        var txt = """
                Запрос на получения временного пропуска для автотранспортного средства.
                                
                🧛: %s
                🏢: %s
                🚗: %s"""
                .formatted(tenant.toString(), tenant.getAddress().toString(), car.toString());
        var approve = InlineKeyboardButton.builder()
                .text("Разрешить")
                .callbackData(accept())
                .build();

        var decline = InlineKeyboardButton.builder()
                .text("Отклонить")
                .callbackData(deny())
                .build();

        var keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(approve, decline))
                .build();
        var msg = new SendMessage();
        msg.setChatId(operatorId);
        msg.setReplyMarkup(keyboard);
        msg.setText(txt);
        return msg;
    }

    private String accept() {
        var accept = new CallbackData(state.getId(), Boolean.TRUE);
        try {
            return mapper.writeValueAsString(accept);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String deny() {
        var accept = new CallbackData(state.getId(), Boolean.FALSE);
        try {
            return mapper.writeValueAsString(accept);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
