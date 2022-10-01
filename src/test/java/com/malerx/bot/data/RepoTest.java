package com.malerx.bot.data;

import com.malerx.bot.data.entity.*;
import com.malerx.bot.data.enums.Role;
import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.repository.OperatorRepository;
import com.malerx.bot.data.repository.StateRepository;
import com.malerx.bot.data.repository.TenantRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Set;

@MicronautTest
public class RepoTest {
    private final OperatorRepository operatorRepository;
    private final StateRepository stateRepository;
    private final TenantRepository tenantRepository;

    public RepoTest(OperatorRepository operatorRepository,
                    StateRepository stateRepository,
                    TenantRepository tenantRepository) {
        this.operatorRepository = operatorRepository;
        this.stateRepository = stateRepository;
        this.tenantRepository = tenantRepository;
    }

    @Test
    public void operatorRepoTest() throws NoSuchAlgorithmException {
        Operator operator = new Operator();
        operator.setId(SecureRandom.getInstanceStrong().nextLong());
        operator.setRole(Role.PASS);
        operator.setActive(Boolean.TRUE);
        operatorRepository.save(operator).join();
    }

    @Test
    public void stateRepoTest() throws NoSuchAlgorithmException {
        State state = new State();
        state.setId(SecureRandom.getInstanceStrong().nextLong());
        state.setStateMachina("registerMachine");
        state.setStage(Stage.PROGRESS);

        stateRepository.save(state).join();
        System.out.println(stateRepository.count().join());
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
                .setId(SecureRandom.getInstanceStrong().nextLong())
                .setName("Vasy")
                .setSurname("Popov")
                .setAddress(address)
                .setCars(Set.of(car));

        tenantRepository.save(tenant).join();
        System.out.println(tenantRepository.count().join());
    }
}
