package ru.urlshortener.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import ru.urlshortener.model.User;

public class UserRepository {

  private final Map<UUID, User> users = new HashMap<>();

  public void save(User user) {
    users.put(user.getId(), user);
  }

  public Optional<User> findById(UUID id) {
    return Optional.ofNullable(users.get(id));
  }

  public Collection<User> findAll() {
    return users.values();
  }

  public boolean existsById(UUID id) {
    return users.containsKey(id);
  }

  public void saveAll(Collection<User> users) {
    this.users.clear();

    for (User user : users) {
      save(user);
    }
  }
}
