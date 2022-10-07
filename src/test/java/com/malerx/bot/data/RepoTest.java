package com.malerx.bot.data;

import com.malerx.bot.data.entity.Address;
import com.malerx.bot.data.entity.Car;
import com.malerx.bot.data.entity.State;
import com.malerx.bot.data.entity.Tenant;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TenantRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class RepoTest {
    private final StateRepository stateRepository;
    private final TenantRepository tenantRepository;

    public RepoTest(StateRepository stateRepository,
                    TenantRepository tenantRepository) {
        this.stateRepository = stateRepository;
        this.tenantRepository = tenantRepository;
    }

    @Test
    public void stateRepoTest() throws NoSuchAlgorithmException {
        State state = new State();
        state.setId(SecureRandom.getInstanceStrong().nextLong())
                .setStage(Stage.PROCEED)
                .setStateMachine("StateMachine")
                .setMessage("Hello");

        stateRepository.save(state).join();

        Collection<State> created = stateRepository.findByIdByStage(state.getId(), Stage.PROCEED).join();
        assertEquals(state.getStage(), created.iterator().next().getStage());
    }

    @Test
    public void userRepoTest() throws NoSuchAlgorithmException {
        Address address = new Address()
                .setStreet("улица Пушкина")
                .setBuild("дом Колотушкина")
                .setApartment("32/12");

        Car car = new Car()
                .setColor("красный")
                .setModel("ВАЗ 2101")
                .setRegNumber("А666МР777");

        Tenant tenant = new Tenant()
                .setName("Vasy")
                .setSurname("Popov")
                .setAddress(address)
                .setCars(Set.of(car));

        tenantRepository.save(tenant).join();

        assertEquals(1L, tenantRepository.count().join());
    }
}
