package ru.urlshortener.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileStorage {

  private final Path storagePath;

  private final Gson gson =
      new GsonBuilder()
          .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
          .setPrettyPrinting()
          .create();

  public FileStorage(Path storagePath) {
    this.storagePath = storagePath;
  }

  public StorageData load() {
    if (!Files.exists(storagePath)) {
      return new StorageData();
    }

    try (Reader reader = Files.newBufferedReader(storagePath)) {
      StorageData data = gson.fromJson(reader, StorageData.class);

      return data == null ? new StorageData() : data;

    } catch (IOException | JsonParseException e) {
      throw new IllegalStateException("Не удалось загрузить данные из JSON", e);
    }
  }

  public void save(StorageData data) {
    try {
      Path parent = storagePath.getParent();

      if (parent != null) {
        Files.createDirectories(parent);
      }

      Path tempPath = storagePath.resolveSibling(storagePath.getFileName() + ".tmp");

      try (Writer writer = Files.newBufferedWriter(tempPath)) {
        gson.toJson(data, writer);
      }

      try {
        Files.move(
            tempPath,
            storagePath,
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
      } catch (java.nio.file.AtomicMoveNotSupportedException e) {
        Files.move(tempPath, storagePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      }

    } catch (IOException e) {
      throw new IllegalStateException("Не удалось сохранить данные в JSON", e);
    }
  }

  private static class LocalDateTimeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(FORMATTER.format(value));
      }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
      if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      return LocalDateTime.parse(in.nextString(), FORMATTER);
    }
  }
}
