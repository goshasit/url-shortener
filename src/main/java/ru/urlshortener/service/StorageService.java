package ru.urlshortener.service;

import ru.urlshortener.repository.LinkRepository;
import ru.urlshortener.repository.UserRepository;
import ru.urlshortener.storage.FileStorage;
import ru.urlshortener.storage.StorageData;

public class StorageService {

  private final FileStorage fileStorage;
  private final UserRepository userRepository;
  private final LinkRepository linkRepository;

  public StorageService(
      FileStorage fileStorage, UserRepository userRepository, LinkRepository linkRepository) {
    this.fileStorage = fileStorage;
    this.userRepository = userRepository;
    this.linkRepository = linkRepository;
  }

  public void load() {
    StorageData data = fileStorage.load();

    userRepository.saveAll(data.getUsers());
    linkRepository.saveAll(data.getLinks());
  }

  public void save() {
    StorageData data = new StorageData();

    data.getUsers().addAll(userRepository.findAll());
    data.getLinks().addAll(linkRepository.findAll());

    fileStorage.save(data);
  }
}
