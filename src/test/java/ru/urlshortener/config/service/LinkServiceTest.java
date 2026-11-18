
package ru.urlshortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urlshortener.config.AppConfig;
import ru.urlshortener.model.ShortLink;
import ru.urlshortener.model.User;
import ru.urlshortener.repository.LinkRepository;
import ru.urlshortener.repository.UserRepository;
import ru.urlshortener.util.LinkGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {

    private LinkRepository linkRepository;
    private UserRepository userRepository;
    private LinkService linkService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        AppConfig config = AppConfig.load();

        linkRepository = new LinkRepository();
        userRepository = new UserRepository();

        userId = UUID.randomUUID();

        userRepository.save(
                new User(userId, LocalDateTime.now())
        );

        linkService = new LinkService(
                linkRepository,
                userRepository,
                new LinkGenerator(config.getCodeLength()),
                config
        );
    }

    @Test
    void shouldCreateLink() {
        ShortLink link = linkService.createLink(
                userId,
                "https://example.com"
        );

        assertNotNull(link.getId());
        assertNotNull(link.getShortCode());
        assertEquals("https://example.com", link.getOriginalUrl());
        assertEquals(userId, link.getUserId());
        assertTrue(linkRepository.existsByCode(link.getShortCode()));
    }

    @Test
    void shouldSetDefaultClickLimit() {
        ShortLink link = linkService.createLink(
                userId,
                "https://example.com"
        );

        assertEquals(10, link.getMaxClicks());
    }

    @Test
    void shouldSetExpirationDate() {
        LocalDateTime beforeCreation = LocalDateTime.now();

        ShortLink link = linkService.createLink(
                userId,
                "https://example.com"
        );

        assertTrue(link.getExpiresAt().isAfter(beforeCreation));
        assertEquals(24, java.time.Duration.between(
                link.getCreatedAt(),
                link.getExpiresAt()
        ).toHours());
    }

    @Test
    void shouldRejectUnknownUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.createLink(
                        UUID.randomUUID(),
                        "https://example.com"
                )
        );
    }

    @Test
    void shouldRejectEmptyUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.createLink(userId, " ")
        );
    }

    @Test
    void shouldFindOwnLink() {
        ShortLink created = linkService.createLink(
                userId,
                "https://example.com"
        );

        ShortLink found = linkService.getLink(
                userId,
                created.getShortCode()
        );

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void shouldRejectAccessToAnotherUsersLink() {
        ShortLink created = linkService.createLink(
                userId,
                "https://example.com"
        );

        assertThrows(
                SecurityException.class,
                () -> linkService.getLink(
                        UUID.randomUUID(),
                        created.getShortCode()
                )
        );
    }

    @Test
    void shouldDeleteOwnLink() {
        ShortLink created = linkService.createLink(
                userId,
                "https://example.com"
        );

        linkService.deleteLink(userId, created.getShortCode());

        assertFalse(
                linkRepository.existsByCode(created.getShortCode())
        );
    }

    @Test
    void shouldRejectDeletingAnotherUsersLink() {
        ShortLink created = linkService.createLink(
                userId,
                "https://example.com"
        );

        assertThrows(
                SecurityException.class,
                () -> linkService.deleteLink(
                        UUID.randomUUID(),
                        created.getShortCode()
                )
        );

        assertTrue(
                linkRepository.existsByCode(created.getShortCode())
        );
    }

    @Test
    void shouldReturnOnlyOwnLinks() {
        linkService.createLink(userId, "https://example.com");
        linkService.createLink(userId, "https://google.com");

        UUID anotherUserId = UUID.randomUUID();

        userRepository.save(
                new User(anotherUserId, LocalDateTime.now())
        );

        linkService.createLink(
                anotherUserId,
                "https://github.com"
        );

        assertEquals(2, linkService.getUserLinks(userId).size());
    }
}
