package io.whitefox.core.services;

import io.whitefox.core.actions.CreateStorage;
import io.whitefox.core.Storage;
import io.whitefox.persistence.StorageManager;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.util.Optional;

@ApplicationScoped
public class StorageService {
  private final StorageManager storageManager;
  private final Clock clock;

  public StorageService(StorageManager storageManager, Clock clock) {
    this.storageManager = storageManager;
    this.clock = clock;
  }

  public Storage createStorage(CreateStorage storage) {
    return storageManager.createStorage(validate(storage));
  }

  public Optional<Storage> getStorage(String name) {
    return storageManager.getStorage(name);
  }

  public Storage validate(CreateStorage storage) {
    if (storage.skipValidation()) {
      return new Storage(
          storage.name(),
          storage.comment(),
          storage.currentUser(),
          storage.type(),
          Optional.empty(),
          storage.uri(),
          clock.millis(),
          storage.currentUser(),
          clock.millis(),
          storage.currentUser(),
          storage.properties());
    } else {
      return new Storage(
          storage.name(),
          storage.comment(),
          storage.currentUser(),
          storage.type(),
          Optional.of(clock.millis()),
          storage.uri(),
          clock.millis(),
          storage.currentUser(),
          clock.millis(),
          storage.currentUser(),
          storage.properties());
    }
  }
}
