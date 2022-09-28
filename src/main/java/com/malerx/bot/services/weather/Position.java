package com.malerx.bot.services.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Singleton
@Slf4j
class Position {
    private final ObjectMapper mapper;

    public Position(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<Coordinates> extract(@NonNull String json) {
        Coordinates result = null;
        try {
            Map<String, Object> response = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> level1 = ((Map<String, Object>) response.get("response"));
            Map<String, Object> level2 = ((Map<String, Object>) level1.get("GeoObjectCollection"));
            Collection<Object> level3 = ((Collection) level2.get("featureMember"));
            if (CollectionUtils.isEmpty(level3)) {
                log.error("extract() -> not found pos");
                return Optional.empty();
            }
            Map<String, Object> level4 = ((Map<String, Object>) level3.iterator().next());
            Map<String, Object> level5 = ((Map<String, Object>) level4.get("GeoObject"));
            Map<String, Object> point = ((Map<String, Object>) level5.get("Point"));
            String[] pos = point.get("pos").toString().split("\\s");
            log.debug("extract() -> extract pos from body response: {}", Arrays.toString(pos));
            result = Coordinates.builder()
                    .longitude(pos[0])
                    .latitude(pos[1])
                    .build();
        } catch (JsonProcessingException e) {
            log.error("extract() -> filed convert response", e);
        }
        return Optional.ofNullable(result);
    }
}
