package com.malerx.bot.factory.stm;

import com.malerx.bot.data.entity.PersistState;
import com.malerx.bot.data.repository.*;
import com.malerx.bot.handlers.state.nsm.FirstStepRegister;
import com.malerx.bot.handlers.state.nsm.SecondStepRegister;
import com.malerx.bot.handlers.state.nsm.State;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;

@Singleton
@Slf4j
public class RegisterStateFactory implements StateFactory {
    private final AddressRepository addressRepository;
    private final CarRepository carRepository;
    private final PgpRepository pgpRepository;
    private final StateRepository stateRepository;
    private final TenantRepository tenantRepository;
    private final TGUserRepository userRepository;

    public RegisterStateFactory(AddressRepository addressRepository,
                                CarRepository carRepository,
                                PgpRepository pgpRepository,
                                StateRepository stateRepository,
                                TenantRepository tenantRepository,
                                TGUserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.carRepository = carRepository;
        this.pgpRepository = pgpRepository;
        this.stateRepository = stateRepository;
        this.tenantRepository = tenantRepository;
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
        }
        return null;
    }
}
