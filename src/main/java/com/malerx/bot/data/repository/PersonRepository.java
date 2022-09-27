package com.malerx.bot.data.repository;

import com.malerx.bot.data.entity.Person;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.async.AsyncCrudRepository;

@Repository
public interface PersonRepository extends AsyncCrudRepository<Person, Long> {
}
