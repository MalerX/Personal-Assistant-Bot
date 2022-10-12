package com.malerx.bot.factory.stm;

import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.handlers.state.nsm.State;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface StateFactory {
    State createState(PersistState persistState, Update update);
}
