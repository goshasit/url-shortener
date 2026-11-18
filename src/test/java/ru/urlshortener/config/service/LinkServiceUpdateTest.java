
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

class LinkServiceUpdateTest {

    private LinkService linkService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        AppConfig config = AppConfig.load();

        UserRepository userRepository = new UserRepository();
        LinkRepository linkRepository = new LinkRepository();

        userId = UUID.randomUUID();

        userRepository.save(new User(
                userId,
                LocalDateTime.now()
        ));

        linkService = new LinkService(
                linkRepository,
                userRepository,
                new LinkGenerator(config.getCodeLength()),
                config
        );
    }

    @Test
    void shouldCreateLinkWithValidHttpsUrl() {
        ShortLink link = linkService.createLink(
                userId,
                "https://google.com"
        );

        assertNotNull(link);
        assertEquals("https://google.com", link.getOriginalUrl());
    }

    @Test
    void shouldRejectUrlWithoutProtocol() {
        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.createLink(userId, "google.com")
        );
    }

    @Test
    void shouldRejectUnsupportedProtocol() {
        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.createLink(userId, "ftp://example.com")
        );
    }

    @Test
    void shouldUpdateMaxClicks() {
        ShortLink created = linkService.createLink(
                userId,
                "https://google.com"
        );

        ShortLink updated = linkService.updateMaxClicks(
                userId,
                created.getShortCode(),
                20
        );

        assertEquals(20, updated.getMaxClicks());
    }

    @Test
    void shouldRejectMaxClicksBelowCurrentClicks() {
        ShortLink link = linkService.createLink(
                userId,
                "https://google.com"
        );

        link.setCurrentClicks(5);

        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.updateMaxClicks(
                        userId,
                        link.getShortCode(),
                        4
                )
        );
    }

    @Test
    void shouldRejectZeroMaxClicks() {
        ShortLink link = linkService.createLink(
                userId,
                "https://google.com"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.updateMaxClicks(
                        userId,
                        link.getShortCode(),
                        0
                )
        );
    }

    @Test
    void shouldUpdateTtl() {
        ShortLink link = linkService.createLink(
                userId,
                "https://google.com"
        );

        LocalDateTime beforeUpdate = LocalDateTime.now();

        ShortLink updated = linkService.updateTtl(
                userId,
                link.getShortCode(),
                48
        );

        assertTrue(
                updated.getExpiresAt().isAfter(
                        beforeUpdate.plusHours(47)
                )
        );
    }

    @Test
    void shouldRejectZeroTtl() {
        ShortLink link = linkService.createLink(
                userId,
                "https://google.com"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.updateTtl(
                        userId,
                        link.getShortCode(),
                        0
                )
        );
    }

    @Test
    void shouldPreventAnotherUserFromUpdatingLink() {
        ShortLink link = linkService.createLink(
                userId,
                "https://google.com"
        );

        UUID anotherUserId = UUID.randomUUID();

        assertThrows(
                SecurityException.class,
                () -> linkService.updateMaxClicks(
                        anotherUserId,
                        link.getShortCode(),
                        20
                )
        );
    }
}
