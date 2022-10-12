package com.malerx.bot.handlers;

import com.malerx.bot.data.entity.PersistState;
import org.telegram.telegrambots.meta.api.objects.Update;

public record Operation(Update update, PersistState state) {
}
