package com.malerx.bot.data.repository;

import com.malerx.bot.data.entity.TGUser;
import com.malerx.bot.data.enums.Role;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.async.AsyncCrudRepository;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Repository
public interface TGUserRepository extends AsyncCrudRepository<TGUser, Long> {
    CompletableFuture<Collection<TGUser>> findByRole(Role role);
}
