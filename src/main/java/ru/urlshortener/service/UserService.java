package ru.urlshortener.service;

import java.time.LocalDateTime;
import java.util.UUID;
import ru.urlshortener.model.User;
import ru.urlshortener.repository.UserRepository;

public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User createUser() {
    User user = new User(UUID.randomUUID(), LocalDateTime.now());

    userRepository.save(user);

    return user;
  }

  public User getUser(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));
  }

  public boolean userExists(UUID id) {
    return userRepository.existsById(id);
  }
}
