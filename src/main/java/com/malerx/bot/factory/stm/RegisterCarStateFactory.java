package com.malerx.bot.factory.stm;

import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.state.nsm.State;
import com.malerx.bot.handlers.state.nsm.register.car.FirstStepCarRegistration;
import com.malerx.bot.handlers.state.nsm.register.car.SecondStepCarRegistration;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;

@Singleton
@Slf4j
public class RegisterCarStateFactory implements StateFactory {
    private final TGUserRepository userRepository;
    private final StateRepository stateRepository;

    public RegisterCarStateFactory(TGUserRepository userRepository,
                                   StateRepository stateRepository) {
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
    }

    @Override
    public State createState(PersistState state, Update update) {
        var step = state.getStep();
        switch (step) {
            case ONE -> {
                return new FirstStepCarRegistration(update, state, userRepository, stateRepository);
            }
            case TWO -> {
                return new SecondStepCarRegistration(update, state, stateRepository);
            }
            default -> {
                return null;
            }
        }
    }
}
