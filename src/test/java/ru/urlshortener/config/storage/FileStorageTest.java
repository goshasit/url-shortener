package ru.urlshortener.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.urlshortener.model.ShortLink;
import ru.urlshortener.model.User;

class FileStorageTest {

  @TempDir Path tempDir;

  @Test
  void shouldSaveAndLoadData() {
    Path file = tempDir.resolve("storage.json");
    FileStorage storage = new FileStorage(file);

    UUID userId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    StorageData original = new StorageData();

    original.getUsers().add(new User(userId, now));

    original
        .getLinks()
        .add(
            new ShortLink(
                UUID.randomUUID(),
                userId,
                "https://example.com",
                "abc123",
                10,
                now,
                now.plusHours(24)));

    storage.save(original);

    StorageData loaded = storage.load();

    assertEquals(1, loaded.getUsers().size());
    assertEquals(1, loaded.getLinks().size());

    assertEquals(userId, loaded.getUsers().get(0).getId());

    assertEquals(now, loaded.getLinks().get(0).getCreatedAt());
  }

  @Test
  void shouldReturnEmptyDataWhenFileDoesNotExist() {
    FileStorage storage = new FileStorage(tempDir.resolve("missing.json"));

    StorageData data = storage.load();

    assertTrue(data.getUsers().isEmpty());
    assertTrue(data.getLinks().isEmpty());
  }
}
