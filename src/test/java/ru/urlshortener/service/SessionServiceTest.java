package ru.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urlshortener.model.User;
import ru.urlshortener.repository.UserRepository;

class SessionServiceTest {

  private UserService userService;
  private SessionService sessionService;

  @BeforeEach
  void setUp() {
    UserRepository repository = new UserRepository();
    userService = new UserService(repository);
    sessionService = new SessionService(userService);
  }

  @Test
  void shouldCreateUserAndLogin() {
    User user = sessionService.createAndLogin();

    assertTrue(sessionService.isLoggedIn());
    assertEquals(user.getId(), sessionService.getCurrentUserId());
  }

  @Test
  void shouldLoginExistingUser() {
    User user = userService.createUser();

    sessionService.login(user.getId());

    assertEquals(user.getId(), sessionService.getCurrentUserId());
  }

  @Test
  void shouldSwitchBetweenUsers() {
    User first = userService.createUser();
    User second = userService.createUser();

    sessionService.login(first.getId());
    assertEquals(first.getId(), sessionService.getCurrentUserId());

    sessionService.login(second.getId());
    assertEquals(second.getId(), sessionService.getCurrentUserId());
  }

  @Test
  void shouldLogout() {
    sessionService.createAndLogin();

    sessionService.logout();

    assertFalse(sessionService.isLoggedIn());
  }

  @Test
  void shouldRejectAccessWithoutLogin() {
    assertThrows(IllegalStateException.class, () -> sessionService.getCurrentUserId());
  }

  @Test
  void shouldRejectUnknownUser() {
    assertThrows(IllegalArgumentException.class, () -> sessionService.login(UUID.randomUUID()));
  }
}
