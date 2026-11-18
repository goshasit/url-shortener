package ru.urlshortener.storage;

import java.util.ArrayList;
import java.util.List;
import ru.urlshortener.model.ShortLink;
import ru.urlshortener.model.User;

public class StorageData {

  private List<User> users = new ArrayList<>();
  private List<ShortLink> links = new ArrayList<>();

  public StorageData() {}

  public List<User> getUsers() {
    return users;
  }

  public List<ShortLink> getLinks() {
    return links;
  }
}
