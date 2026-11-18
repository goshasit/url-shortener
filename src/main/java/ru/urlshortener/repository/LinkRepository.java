
package ru.urlshortener.repository;

import ru.urlshortener.model.ShortLink;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkRepository {

    private final Map<String, ShortLink> links = new ConcurrentHashMap<>();

    public void save(ShortLink link) {
        links.put(link.getShortCode(), link);
    }

    public Optional<ShortLink> findByCode(String shortCode) {
        return Optional.ofNullable(links.get(shortCode));
    }

    public Collection<ShortLink> findAll() {
        return List.copyOf(links.values());
    }

    public Collection<ShortLink> findAllByUserId(UUID userId) {
        return links.values()
                .stream()
                .filter(link -> link.getUserId().equals(userId))
                .toList();
    }

    public boolean existsByCode(String shortCode) {
        return links.containsKey(shortCode);
    }

    public void deleteByCode(String shortCode) {
        links.remove(shortCode);
    }

    public int deleteExpired(LocalDateTime now) {
        int previousSize = links.size();

        links.entrySet().removeIf(entry ->
                !entry.getValue().getExpiresAt().isAfter(now)
        );

        return previousSize - links.size();
    }

    public int count() {
        return links.size();
    }

    public void saveAll(Collection<ShortLink> links) {
        this.links.clear();

        for (ShortLink link : links) {
            save(link);
        }
    }
}
