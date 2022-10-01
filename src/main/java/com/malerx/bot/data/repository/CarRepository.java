package com.malerx.bot.data.repository;

import com.malerx.bot.data.entity.Car;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.async.AsyncCrudRepository;

@Repository
public interface CarRepository extends AsyncCrudRepository<Car, Long> {
}
