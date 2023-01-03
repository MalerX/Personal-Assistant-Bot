package com.malerx.bot.factory.stm;

import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.repository.CarRepository;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TGUserRepository;
import com.malerx.bot.handlers.state.nsm.State;
import com.malerx.bot.handlers.state.nsm.pass.FirstStepGettingPassState;
import com.malerx.bot.handlers.state.nsm.pass.SecondStepGettingPassState;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;

@Singleton
@Slf4j
public class GettingPassStateFactory implements StateFactory {
    private final TGUserRepository userRepository;
    private final StateRepository stateRepository;
    private final CarRepository carRepository;

    public GettingPassStateFactory(TGUserRepository userRepository,
                                   StateRepository stateRepository,
                                   CarRepository carRepository) {
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
        this.carRepository = carRepository;
    }

    @Override
    public State createState(PersistState persistState, Update update) {
        var step = persistState.getStep();
        switch (step) {
            case ONE -> {
                return new FirstStepGettingPassState(update, persistState, userRepository, stateRepository);
            }
            case TWO -> {
                return new SecondStepGettingPassState(carRepository, update);
            }
            default -> {
                return null;
            }
        }
    }
}
