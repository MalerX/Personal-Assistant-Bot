package com.malerx.bot.data.repository;

import com.malerx.bot.data.entity.Operator;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.async.AsyncCrudRepository;

@Repository
public interface OperatorRepository extends AsyncCrudRepository<Operator, Long> {
}
