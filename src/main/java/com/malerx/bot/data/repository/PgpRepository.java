package com.malerx.bot.data.repository;

import com.malerx.bot.data.entity.GpgRecord;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.async.AsyncCrudRepository;

@Repository
public interface PgpRepository extends AsyncCrudRepository<GpgRecord, Long> {
}
