
package ru.urlshortener.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urlshortener.model.ShortLink;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkRepositoryTest {

    private LinkRepository repository;
    private UUID userId;

    @BeforeEach
    void setUp() {
        repository = new LinkRepository();
        userId = UUID.randomUUID();
    }

    private ShortLink createLink(String code, UUID ownerId) {
        return new ShortLink(
                UUID.randomUUID(),
                ownerId,
                "https://example.com",
                code,
                10,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24)
        );
    }

    @Test
    void shouldSaveAndFindLink() {
        ShortLink link = createLink("abc123", userId);

        repository.save(link);

        assertTrue(repository.findByCode("abc123").isPresent());
        assertEquals(link, repository.findByCode("abc123").orElseThrow());
    }

    @Test
    void shouldReturnEmptyForUnknownCode() {
        assertTrue(repository.findByCode("unknown").isEmpty());
    }

    @Test
    void shouldFindLinksByUserId() {
        ShortLink first = createLink("abc123", userId);
        ShortLink second = createLink("def456", userId);
        ShortLink other = createLink("xyz789", UUID.randomUUID());

        repository.save(first);
        repository.save(second);
        repository.save(other);

        assertEquals(2, repository.findAllByUserId(userId).size());
    }

    @Test
    void shouldDeleteLink() {
        repository.save(createLink("abc123", userId));

        repository.deleteByCode("abc123");

        assertFalse(repository.existsByCode("abc123"));
    }

    @Test
    void shouldCountLinks() {
        repository.save(createLink("abc123", userId));
        repository.save(createLink("def456", userId));

        assertEquals(2, repository.count());
    }

    @Test
    void shouldCheckIfCodeExists() {
        repository.save(createLink("abc123", userId));

        assertTrue(repository.existsByCode("abc123"));
        assertFalse(repository.existsByCode("unknown"));
    }
}
