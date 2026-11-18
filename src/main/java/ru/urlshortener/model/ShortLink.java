package ru.urlshortener.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {

  private UUID id;
  private UUID userId;

  private String originalUrl;
  private String shortCode;

  private int maxClicks;
  private int currentClicks;

  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;

  public ShortLink() {}

  public ShortLink(
      UUID id,
      UUID userId,
      String originalUrl,
      String shortCode,
      int maxClicks,
      LocalDateTime createdAt,
      LocalDateTime expiresAt) {
    this.id = id;
    this.userId = userId;
    this.originalUrl = originalUrl;
    this.shortCode = shortCode;
    this.maxClicks = maxClicks;
    this.currentClicks = 0;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getOriginalUrl() {
    return originalUrl;
  }

  public String getShortCode() {
    return shortCode;
  }

  public int getMaxClicks() {
    return maxClicks;
  }

  public int getCurrentClicks() {
    return currentClicks;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setMaxClicks(int maxClicks) {
    this.maxClicks = maxClicks;
  }

  public void setCurrentClicks(int currentClicks) {
    this.currentClicks = currentClicks;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }
}
