package ru.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urlshortener.model.User;
import ru.urlshortener.repository.UserRepository;

class UserServiceTest {

  private UserRepository userRepository;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userRepository = new UserRepository();
    userService = new UserService(userRepository);
  }

  @Test
  void shouldCreateUser() {
    User user = userService.createUser();

    assertNotNull(user.getId());
    assertNotNull(user.getCreatedAt());
    assertTrue(userRepository.existsById(user.getId()));
  }

  @Test
  void shouldReturnExistingUser() {
    User createdUser = userService.createUser();

    User foundUser = userService.getUser(createdUser.getId());

    assertEquals(createdUser.getId(), foundUser.getId());
  }

  @Test
  void shouldThrowExceptionForUnknownUser() {
    assertThrows(
        IllegalArgumentException.class, () -> userService.getUser(java.util.UUID.randomUUID()));
  }

  @Test
  void shouldConfirmUserExists() {
    User user = userService.createUser();

    assertTrue(userService.userExists(user.getId()));
  }

  @Test
  void shouldConfirmUnknownUserDoesNotExist() {
    assertFalse(userService.userExists(java.util.UUID.randomUUID()));
  }
}
