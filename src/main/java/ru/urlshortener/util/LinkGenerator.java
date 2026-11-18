package ru.urlshortener.util;

import java.security.SecureRandom;

public class LinkGenerator {

  private static final String CHARACTERS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  private final SecureRandom random = new SecureRandom();
  private final int codeLength;

  public LinkGenerator(int codeLength) {
    if (codeLength < 4 || codeLength > 12) {
      throw new IllegalArgumentException("Длина кода должна быть от 4 до 12 символов");
    }

    this.codeLength = codeLength;
  }

  public String generate() {
    StringBuilder code = new StringBuilder(codeLength);

    for (int i = 0; i < codeLength; i++) {
      int index = random.nextInt(CHARACTERS.length());
      code.append(CHARACTERS.charAt(index));
    }

    return code.toString();
  }
}
