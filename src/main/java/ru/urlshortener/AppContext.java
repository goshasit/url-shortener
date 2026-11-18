package ru.urlshortener;

import ru.urlshortener.repository.LinkRepository;
import ru.urlshortener.repository.UserRepository;
import ru.urlshortener.service.LinkCleanupService;
import ru.urlshortener.service.LinkService;
import ru.urlshortener.service.NotificationService;
import ru.urlshortener.service.SessionService;
import ru.urlshortener.service.StorageService;
import ru.urlshortener.service.UserService;
import ru.urlshortener.storage.FileStorage;
import ru.urlshortener.util.BrowserUtil;
import ru.urlshortener.util.LinkGenerator;

public class AppContext {

  private final AppConfig config;

  private final UserRepository userRepository;
  private final LinkRepository linkRepository;

  private final UserService userService;
  private final LinkService linkService;
  private final SessionService sessionService;
  private final StorageService storageService;
  private final LinkCleanupService linkCleanupService;

  private final BrowserUtil browserUtil;

  public AppContext() {
    config = AppConfig.load();

    userRepository = new UserRepository();
    linkRepository = new LinkRepository();

    userService = new UserService(userRepository);

    linkService =
        new LinkService(
            linkRepository, userRepository, new LinkGenerator(config.getCodeLength()), config);

    sessionService = new SessionService(userService);

    storageService =
        new StorageService(
            new FileStorage(config.getStoragePath()), userRepository, linkRepository);

    browserUtil = new BrowserUtil();

    NotificationService notificationService = new NotificationService();

    linkCleanupService =
        new LinkCleanupService(
            linkService, storageService, notificationService, config.getCleanupIntervalSeconds());

    storageService.load();
  }

  public LinkService getLinkService() {
    return linkService;
  }

  public UserService getUserService() {
    return userService;
  }

  public SessionService getSessionService() {
    return sessionService;
  }

  public StorageService getStorageService() {
    return storageService;
  }

  public LinkCleanupService getLinkCleanupService() {
    return linkCleanupService;
  }

  public BrowserUtil getBrowserUtil() {
    return browserUtil;
  }

  public AppConfig getConfig() {
    return config;
  }
}
