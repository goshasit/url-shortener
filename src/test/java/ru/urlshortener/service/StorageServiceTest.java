package ru.urlshortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.urlshortener.model.ShortLink;
import ru.urlshortener.model.User;
import ru.urlshortener.repository.LinkRepository;
import ru.urlshortener.repository.UserRepository;
import ru.urlshortener.storage.FileStorage;

class StorageServiceTest {

  @TempDir Path tempDir;

  @Test
  void shouldSaveAndRestoreUsersAndLinks() {
    // Подготавливаем исходные репозитории
    UserRepository originalUserRepository = new UserRepository();
    LinkRepository originalLinkRepository = new LinkRepository();

    // Создаём пользователя
    UUID userId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    User user = new User(userId, now);
    originalUserRepository.save(user);

    // Создаём ссылку
    ShortLink link =
        new ShortLink(
            UUID.randomUUID(), userId, "https://example.com", "abc123", 10, now, now.plusHours(24));

    link.setCurrentClicks(3);
    originalLinkRepository.save(link);

    // Сохраняем данные через StorageService
    Path storagePath = tempDir.resolve("storage.json");

    StorageService originalStorageService =
        new StorageService(
            new FileStorage(storagePath), originalUserRepository, originalLinkRepository);

    originalStorageService.save();

    // Создаём новые пустые репозитории
    UserRepository restoredUserRepository = new UserRepository();
    LinkRepository restoredLinkRepository = new LinkRepository();

    // Загружаем данные из JSON
    StorageService restoredStorageService =
        new StorageService(
            new FileStorage(storagePath), restoredUserRepository, restoredLinkRepository);

    restoredStorageService.load();

    // Проверяем восстановленного пользователя
    assertEquals(1, restoredUserRepository.findAll().size());

    User restoredUser = restoredUserRepository.findById(userId).orElseThrow();

    assertEquals(userId, restoredUser.getId());

    // Проверяем восстановленную ссылку
    assertEquals(1, restoredLinkRepository.findAll().size());

    ShortLink restoredLink = restoredLinkRepository.findByCode("abc123").orElseThrow();

    assertNotNull(restoredLink);
    assertEquals(userId, restoredLink.getUserId());
    assertEquals("https://example.com", restoredLink.getOriginalUrl());
    assertEquals("abc123", restoredLink.getShortCode());
    assertEquals(10, restoredLink.getMaxClicks());
    assertEquals(3, restoredLink.getCurrentClicks());
    assertEquals(now, restoredLink.getCreatedAt());
    assertEquals(now.plusHours(24), restoredLink.getExpiresAt());

    assertTrue(restoredUserRepository.existsById(userId));
  }
}
