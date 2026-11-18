
package ru.urlshortener.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LinkCleanupService {

    private final LinkService linkService;
    private final StorageService storageService;
    private final NotificationService notificationService;
    private final long intervalSeconds;

    private ScheduledExecutorService scheduler;

    public LinkCleanupService(
            LinkService linkService,
            StorageService storageService,
            NotificationService notificationService,
            long intervalSeconds
    ) {
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Интервал очистки должен быть больше нуля"
            );
        }

        this.linkService = linkService;
        this.storageService = storageService;
        this.notificationService = notificationService;
        this.intervalSeconds = intervalSeconds;
    }

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(
                this::cleanup,
                0,
                intervalSeconds,
                TimeUnit.SECONDS
        );
    }

    public void cleanup() {
        try {
            int deletedCount = linkService.cleanupExpiredLinks();

            if (deletedCount > 0) {
                storageService.save();
                notificationService.notifyExpiredLinks(deletedCount);
            }
        } catch (RuntimeException e) {
            System.err.println(
                    "Ошибка автоматической очистки ссылок: "
                            + e.getMessage()
            );
        }
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();

            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
