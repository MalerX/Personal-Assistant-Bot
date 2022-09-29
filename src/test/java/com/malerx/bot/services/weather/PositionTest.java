package com.malerx.bot.services.weather;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class PositionTest {
    @Inject
    private Position position;

    @Test
    public void extractPosTest() throws IOException {
        String weatherJson = new String(FileUtils.readFileToByteArray(new File("src/test/resources/json/position.json")));
        Optional<Coordinates> pos = position.extract(weatherJson);
        assertTrue(pos.isPresent());
//        pos.ifPresent(geo -> {
//            assertEquals("37.617698", geo.getLongitude());
//            assertEquals("55.755864", geo.getLatitude());
//        });
    }
}