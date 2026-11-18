
package ru.urlshortener.service;

import ru.urlshortener.config.AppConfig;
import ru.urlshortener.model.ShortLink;
import ru.urlshortener.repository.LinkRepository;
import ru.urlshortener.repository.UserRepository;
import ru.urlshortener.util.LinkGenerator;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

public class LinkService {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 100;

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final LinkGenerator linkGenerator;
    private final AppConfig config;

    public LinkService(
            LinkRepository linkRepository,
            UserRepository userRepository,
            LinkGenerator linkGenerator,
            AppConfig config
    ) {
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
        this.linkGenerator = linkGenerator;
        this.config = config;
    }

    public ShortLink createLink(UUID userId, String originalUrl) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        validateUrl(originalUrl);

        String shortCode = generateUniqueCode();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(config.getTtlHours());

        ShortLink link = new ShortLink(
                UUID.randomUUID(),
                userId,
                originalUrl.trim(),
                shortCode,
                config.getDefaultMaxClicks(),
                now,
                expiresAt
        );

        linkRepository.save(link);

        return link;
    }

    public ShortLink getLink(UUID userId, String shortCode) {
        ShortLink link = linkRepository.findByCode(shortCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ссылка не найдена")
                );

        checkOwnership(link, userId);

        return link;
    }

    public Collection<ShortLink> getUserLinks(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        return linkRepository.findAllByUserId(userId);
    }

    public void deleteLink(UUID userId, String shortCode) {
        ShortLink link = getLink(userId, shortCode);
        linkRepository.deleteByCode(link.getShortCode());
    }

    public ShortLink updateMaxClicks(
            UUID userId,
            String shortCode,
            int newMaxClicks
    ) {
        ShortLink link = getLink(userId, shortCode);

        if (newMaxClicks <= 0) {
            throw new IllegalArgumentException(
                    "Лимит переходов должен быть больше нуля"
            );
        }

        if (newMaxClicks < link.getCurrentClicks()) {
            throw new IllegalArgumentException(
                    "Новый лимит не может быть меньше уже совершённых переходов"
            );
        }

        link.setMaxClicks(newMaxClicks);

        return link;
    }

    public ShortLink updateTtl(
            UUID userId,
            String shortCode,
            long ttlHours
    ) {
        ShortLink link = getLink(userId, shortCode);

        if (ttlHours <= 0) {
            throw new IllegalArgumentException(
                    "Срок действия должен быть больше нуля"
            );
        }

        link.setExpiresAt(LocalDateTime.now().plusHours(ttlHours));

        return link;
    }

    public int cleanupExpiredLinks() {
        return linkRepository.deleteExpired(LocalDateTime.now());
    }

    public ShortLink accessLink(String shortCode) {
        ShortLink link = linkRepository.findByCode(shortCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ссылка не найдена")
                );

        LocalDateTime now = LocalDateTime.now();

        if (!link.getExpiresAt().isAfter(now)) {
            throw new IllegalStateException(
                    "Срок действия ссылки истёк"
            );
        }

        if (link.getCurrentClicks() >= link.getMaxClicks()) {
            throw new IllegalStateException(
                    "Лимит переходов исчерпан"
            );
        }

        link.setCurrentClicks(link.getCurrentClicks() + 1);

        return link;
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "URL не может быть пустым"
            );
        }

        try {
            URI uri = new URI(url.trim());

            String scheme = uri.getScheme();

            if (scheme == null
                    || !(scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException(
                        "Допустимы только URL с протоколом http или https"
                );
            }

            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException(
                        "URL должен содержать корректное имя хоста"
                );
            }

            if (uri.getPort() > 65535) {
                throw new IllegalArgumentException(
                        "Порт URL не может быть больше 65535"
                );
            }

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Некорректный URL: " + url,
                    e
            );
        }
    }

    private String generateUniqueCode() {
        for (int attempt = 0;
             attempt < MAX_CODE_GENERATION_ATTEMPTS;
             attempt++) {

            String code = linkGenerator.generate();

            if (!linkRepository.existsByCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException(
                "Не удалось сгенерировать уникальный короткий код"
        );
    }

    private void checkOwnership(ShortLink link, UUID userId) {
        if (!link.getUserId().equals(userId)) {
            throw new SecurityException(
                    "У вас нет прав на управление этой ссылкой"
            );
        }
    }
}
