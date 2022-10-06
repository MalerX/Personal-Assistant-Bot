package com.malerx.bot.data.repository;

import com.malerx.bot.data.entity.State;
import com.malerx.bot.data.enums.Stage;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.async.AsyncCrudRepository;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Repository
public interface StateRepository extends AsyncCrudRepository<State, Long> {
    @Query("FROM State s WHERE s.id = :id AND s.stage = :stage")
    CompletableFuture<Collection<State>> findByIdByStage(Long id, Stage stage);
}
