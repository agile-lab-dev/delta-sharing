package io.whitefox.core.services;

import io.whitefox.core.CreateMetastore;
import io.whitefox.core.Metastore;
import io.whitefox.persistence.StorageManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Clock;
import java.util.Optional;

@ApplicationScoped
public class MetastoreService {

    private final StorageManager storageManager;

    private final Clock clock;

    @Inject
    public MetastoreService(StorageManager storageManager, Clock clock) {
        this.storageManager = storageManager;
        this.clock = clock;
    }

    public Metastore createStorageManager(CreateMetastore metastore) {
        return storageManager.createMetastore(validate(metastore));
    }

    public Optional<Metastore> getMetastore(String name) {
        return storageManager.getMetastore(name);
    }


    private Metastore validate(CreateMetastore metastore) {
        // always valid, real impl will throw an exception if not valid
        return new Metastore(
                metastore.name(),
                metastore.comment(),
                metastore.currentUser(),
                metastore.type(),
                metastore.properties(),
                clock.millis(),
                clock.millis(),
                metastore.currentUser(),
                clock.millis(),
                metastore.currentUser()
        );
    }
}
