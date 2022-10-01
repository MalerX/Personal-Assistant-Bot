package com.malerx.bot.data.repository;

import com.malerx.bot.data.entity.Tenant;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.async.AsyncCrudRepository;

@Repository
public interface TenantRepository extends AsyncCrudRepository<Tenant, Long> {
}
