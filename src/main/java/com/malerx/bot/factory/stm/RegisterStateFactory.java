package com.malerx.bot.factory.stm;

import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.repository.*;
import com.malerx.bot.handlers.state.nsm.register.user.FirstStepRegister;
import com.malerx.bot.handlers.state.nsm.register.user.SecondStepRegister;
import com.malerx.bot.handlers.state.nsm.State;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;

@Singleton
@Slf4j
public class RegisterStateFactory implements StateFactory {
    private final StateRepository stateRepository;
    private final TGUserRepository userRepository;

    public RegisterStateFactory(StateRepository stateRepository,
                                TGUserRepository userRepository) {
        this.stateRepository = stateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public State createState(PersistState persistState, Update update) {
        var step = persistState.getStep();
        switch (step) {
            case ONE -> {
                return new FirstStepRegister(update, persistState, stateRepository, userRepository);
            }
            case TWO -> {
                return new SecondStepRegister(update, persistState, stateRepository, userRepository);
            }
            default -> {
                return null;
            }
        }
    }
}
