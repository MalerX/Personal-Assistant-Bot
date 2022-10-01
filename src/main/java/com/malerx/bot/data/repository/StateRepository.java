package com.malerx.bot.data.repository;

import com.malerx.bot.data.entity.State;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.async.AsyncCrudRepository;

@Repository
public interface StateRepository extends AsyncCrudRepository<State, Long> {
}
