package ru.urlshortener.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class AppConfig {

    private final int ttlHours;
    private final int defaultMaxClicks;
    private final int codeLength;
    private final int cleanupIntervalSeconds;
    private final Path storagePath;

    private AppConfig(
            int ttlHours,
            int defaultMaxClicks,
            int codeLength,
            int cleanupIntervalSeconds,
            Path storagePath
    ) {
        this.ttlHours = ttlHours;
        this.defaultMaxClicks = defaultMaxClicks;
        this.codeLength = codeLength;
        this.cleanupIntervalSeconds = cleanupIntervalSeconds;
        this.storagePath = storagePath;
    }

    public static AppConfig load() {
        Properties properties = new Properties();

        try (InputStream input = AppConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "Файл config.properties не найден"
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удалось загрузить конфигурацию", e
            );
        }

        int ttlHours = Integer.parseInt(
                properties.getProperty("link.ttl.hours")
        );

        int defaultMaxClicks = Integer.parseInt(
                properties.getProperty("link.default.max-clicks")
        );

        int codeLength = Integer.parseInt(
                properties.getProperty("link.code.length")
        );

        int cleanupIntervalSeconds = Integer.parseInt(
                properties.getProperty("cleanup.interval.seconds")
        );

        Path storagePath = Path.of(
                properties.getProperty("storage.path")
        );

        validate(
                ttlHours,
                defaultMaxClicks,
                codeLength,
                cleanupIntervalSeconds
        );

        return new AppConfig(
                ttlHours,
                defaultMaxClicks,
                codeLength,
                cleanupIntervalSeconds,
                storagePath
        );
    }

    private static void validate(
            int ttlHours,
            int defaultMaxClicks,
            int codeLength,
            int cleanupIntervalSeconds
    ) {
        if (ttlHours <= 0) {
            throw new IllegalArgumentException(
                    "TTL должен быть больше нуля"
            );
        }

        if (defaultMaxClicks <= 0) {
            throw new IllegalArgumentException(
                    "Лимит переходов должен быть больше нуля"
            );
        }

        if (codeLength < 4 || codeLength > 12) {
            throw new IllegalArgumentException(
                    "Длина короткого кода должна быть от 4 до 12"
            );
        }

        if (cleanupIntervalSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Интервал очистки должен быть больше нуля"
            );
        }
    }

    public int getTtlHours() {
        return ttlHours;
    }

    public int getDefaultMaxClicks() {
        return defaultMaxClicks;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public int getCleanupIntervalSeconds() {
        return cleanupIntervalSeconds;
    }

    public Path getStoragePath() {
        return storagePath;
    }
}