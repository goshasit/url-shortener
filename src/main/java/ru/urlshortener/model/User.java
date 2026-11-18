package ru.urlshortener.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {

  private UUID id;
  private LocalDateTime createdAt;

  public User() {}

  public User(UUID id, LocalDateTime createdAt) {
    this.id = id;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
