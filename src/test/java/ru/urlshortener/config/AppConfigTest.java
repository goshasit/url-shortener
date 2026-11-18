package ru.urlshortener.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void shouldLoadConfiguration() {
        AppConfig config = AppConfig.load();

        assertEquals(24, config.getTtlHours());
        assertEquals(10, config.getDefaultMaxClicks());
        assertEquals(6, config.getCodeLength());
        assertEquals(60, config.getCleanupIntervalSeconds());
        assertEquals(
                java.nio.file.Path.of("./data/storage.json").normalize(),
                config.getStoragePath().normalize()
        );
    }
}