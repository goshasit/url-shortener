package ru.urlshortener.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class BrowserUtil {

  public void open(String url) {
    if (!Desktop.isDesktopSupported()) {
      throw new UnsupportedOperationException("Открытие браузера не поддерживается");
    }

    try {
      Desktop.getDesktop().browse(URI.create(url));
    } catch (IOException e) {
      throw new IllegalStateException("Не удалось открыть ссылку в браузере", e);
    }
  }
}
