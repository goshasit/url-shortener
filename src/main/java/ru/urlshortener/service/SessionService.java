package ru.urlshortener.service;

import java.util.UUID;
import ru.urlshortener.model.User;

public class SessionService {

  private final UserService userService;

  private UUID currentUserId;

  public SessionService(UserService userService) {
    this.userService = userService;
  }

  public User createAndLogin() {
    User user = userService.createUser();

    currentUserId = user.getId();

    return user;
  }

  public User login(UUID userId) {
    User user = userService.getUser(userId);

    currentUserId = user.getId();

    return user;
  }

  public UUID getCurrentUserId() {
    if (currentUserId == null) {
      throw new IllegalStateException("Сначала необходимо выбрать пользователя");
    }

    return currentUserId;
  }

  public boolean isLoggedIn() {
    return currentUserId != null;
  }

  public void logout() {
    currentUserId = null;
  }
}
