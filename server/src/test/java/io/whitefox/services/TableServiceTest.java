package io.whitefox.services;

import static org.junit.jupiter.api.Assertions.*;

import io.whitefox.core.*;
import io.whitefox.core.actions.CreateInternalTable;
import io.whitefox.core.actions.CreateMetastore;
import io.whitefox.core.actions.CreateProvider;
import io.whitefox.core.actions.CreateStorage;
import io.whitefox.core.services.MetastoreService;
import io.whitefox.core.services.ProviderService;
import io.whitefox.core.services.StorageService;
import io.whitefox.core.services.TableService;
import io.whitefox.core.services.exceptions.AlreadyExists;
import io.whitefox.core.services.exceptions.ProviderNotFound;
import io.whitefox.persistence.memory.InMemoryStorageManager;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TableServiceTest {

  @Test
  public void createFirstDeltaTable() {
    var storageManager = new InMemoryStorageManager();
    var clock = Clock.fixed(Instant.ofEpochMilli(9), ZoneOffset.UTC);
    var metastoreService = new MetastoreService(storageManager, clock);
    var storageService = new StorageService(storageManager, clock);
    var providerService =
        new ProviderService(storageManager, metastoreService, storageService, clock);
    var principal = new Principal("Mr. Fox");
    var target = new TableService(storageManager, clock, providerService);
    createMetastore(metastoreService, principal);
    createStorage(storageService, principal);
    providerService.createProvider(
        new CreateProvider("provider1", "storage1", Optional.empty(), principal));
    var result = target.createInternalTable(
        "provider1",
        principal,
        new CreateInternalTable(
            "deltaTable",
            Optional.empty(),
            false,
            new InternalTable.DeltaTableProperties("s3://bucket/delta-table")));
    assertEquals(result.name(), "deltaTable");
    assertEquals(result.comment(), Optional.empty());
    assertEquals(
        result.properties(), new InternalTable.DeltaTableProperties("s3://bucket/delta-table"));
    assertEquals(result.validatedAt(), Optional.of(9L));
    assertEquals(result.createdAt(), 9L);
    assertEquals(result.createdBy(), principal);
    assertEquals(result.updatedAt(), 9L);
    assertEquals(result.updatedBy(), principal);
    assertEquals(result.provider(), providerService.getProvider("provider1").get());
  }

  @Test
  public void createIcebergTableWithoutMetastore() {
    var storageManager = new InMemoryStorageManager();
    var clock = Clock.fixed(Instant.ofEpochMilli(9), ZoneOffset.UTC);
    var metastoreService = new MetastoreService(storageManager, clock);
    var storageService = new StorageService(storageManager, clock);
    var providerService =
        new ProviderService(storageManager, metastoreService, storageService, clock);
    var principal = new Principal("Mr. Fox");
    var target = new TableService(storageManager, clock, providerService);
    createMetastore(metastoreService, principal);
    createStorage(storageService, principal);
    providerService.createProvider(
        new CreateProvider("provider1", "storage1", Optional.empty(), principal));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> target.createInternalTable(
            "provider1",
            principal,
            new CreateInternalTable(
                "icebergTable",
                Optional.empty(),
                false,
                new InternalTable.IcebergTableProperties("dbName", "tName"))));

    Assertions.assertDoesNotThrow(() -> target.createInternalTable(
        "provider1",
        principal,
        new CreateInternalTable(
            "icebergTable",
            Optional.empty(),
            true,
            new InternalTable.IcebergTableProperties("dbName", "tName"))));
  }

  @Test
  public void createIcebergTable() {
    var storageManager = new InMemoryStorageManager();
    var clock = Clock.fixed(Instant.ofEpochMilli(9), ZoneOffset.UTC);
    var metastoreService = new MetastoreService(storageManager, clock);
    var storageService = new StorageService(storageManager, clock);
    var providerService =
        new ProviderService(storageManager, metastoreService, storageService, clock);
    var principal = new Principal("Mr. Fox");
    var target = new TableService(storageManager, clock, providerService);
    createMetastore(metastoreService, principal);
    createStorage(storageService, principal);
    providerService.createProvider(
        new CreateProvider("provider1", "storage1", Optional.of("metastore1"), principal));
    var result = target.createInternalTable(
        "provider1",
        principal,
        new CreateInternalTable(
            "icebergTable",
            Optional.empty(),
            false,
            new InternalTable.IcebergTableProperties("dbName", "tn")));
    assertEquals(result.name(), "icebergTable");
    assertEquals(result.comment(), Optional.empty());
    assertEquals(result.properties(), new InternalTable.IcebergTableProperties("dbName", "tn"));
    assertEquals(result.validatedAt(), Optional.of(9L));
    assertEquals(result.createdAt(), 9L);
    assertEquals(result.createdBy(), principal);
    assertEquals(result.updatedAt(), 9L);
    assertEquals(result.updatedBy(), principal);
    assertEquals(result.provider(), providerService.getProvider("provider1").get());
  }

  @Test
  public void failToCreateExistingTables() {
    var storageManager = new InMemoryStorageManager();
    var clock = Clock.fixed(Instant.ofEpochMilli(9), ZoneOffset.UTC);
    var metastoreService = new MetastoreService(storageManager, clock);
    var storageService = new StorageService(storageManager, clock);
    var providerService =
        new ProviderService(storageManager, metastoreService, storageService, clock);
    var principal = new Principal("Mr. Fox");
    var target = new TableService(storageManager, clock, providerService);
    createMetastore(metastoreService, principal);
    createStorage(storageService, principal);
    providerService.createProvider(
        new CreateProvider("provider1", "storage1", Optional.of("metastore1"), principal));
    target.createInternalTable(
        "provider1",
        principal,
        new CreateInternalTable(
            "icebergTable",
            Optional.empty(),
            false,
            new InternalTable.IcebergTableProperties("dbName", "tn")));
    Assertions.assertThrows(
        AlreadyExists.class,
        () -> target.createInternalTable(
            "provider1",
            principal,
            new CreateInternalTable(
                "icebergTable",
                Optional.empty(),
                false,
                new InternalTable.IcebergTableProperties("dbName", "tn"))));
  }

  @Test
  public void failToCreateWhenProviderDoesNotExist() {
    var storageManager = new InMemoryStorageManager();
    var clock = Clock.fixed(Instant.ofEpochMilli(9), ZoneOffset.UTC);
    var metastoreService = new MetastoreService(storageManager, clock);
    var storageService = new StorageService(storageManager, clock);
    var providerService =
        new ProviderService(storageManager, metastoreService, storageService, clock);
    var principal = new Principal("Mr. Fox");
    var target = new TableService(storageManager, clock, providerService);
    assertThrows(
        ProviderNotFound.class,
        () -> target.createInternalTable(
            "provider1",
            principal,
            new CreateInternalTable(
                "icebergTable",
                Optional.empty(),
                false,
                new InternalTable.IcebergTableProperties("dbName", "tn"))));
  }

  @Test
  public void describeExistingTable() {
    var storageManager = new InMemoryStorageManager();
    var clock = Clock.fixed(Instant.ofEpochMilli(9), ZoneOffset.UTC);
    var metastoreService = new MetastoreService(storageManager, clock);
    var storageService = new StorageService(storageManager, clock);
    var providerService =
        new ProviderService(storageManager, metastoreService, storageService, clock);
    var principal = new Principal("Mr. Fox");
    var target = new TableService(storageManager, clock, providerService);
    createMetastore(metastoreService, principal);
    createStorage(storageService, principal);
    providerService.createProvider(
        new CreateProvider("provider1", "storage1", Optional.of("metastore1"), principal));
    target.createInternalTable(
        "provider1",
        principal,
        new CreateInternalTable(
            "icebergTable",
            Optional.empty(),
            false,
            new InternalTable.IcebergTableProperties("dbName", "tn")));
    var result = target.getInternalTable("provider1", "icebergTable").get();
    assertEquals(result.name(), "icebergTable");
    assertEquals(result.comment(), Optional.empty());
    assertEquals(result.properties(), new InternalTable.IcebergTableProperties("dbName", "tn"));
    assertEquals(result.validatedAt(), Optional.of(9L));
    assertEquals(result.createdAt(), 9L);
    assertEquals(result.createdBy(), principal);
    assertEquals(result.updatedAt(), 9L);
    assertEquals(result.updatedBy(), principal);
    assertEquals(result.provider(), providerService.getProvider("provider1").get());
  }

  @Test
  public void describeNotExistingProvider() {
    var storageManager = new InMemoryStorageManager();
    var clock = Clock.fixed(Instant.ofEpochMilli(9), ZoneOffset.UTC);
    var metastoreService = new MetastoreService(storageManager, clock);
    var storageService = new StorageService(storageManager, clock);
    var providerService =
        new ProviderService(storageManager, metastoreService, storageService, clock);
    var target = new TableService(storageManager, clock, providerService);
    assertThrows(
        ProviderNotFound.class, () -> target.getInternalTable("provider1", "icebergTable"));
  }

  @Test
  public void describeNotExistingTableInProvider() {
    var storageManager = new InMemoryStorageManager();
    var clock = Clock.fixed(Instant.ofEpochMilli(9), ZoneOffset.UTC);
    var metastoreService = new MetastoreService(storageManager, clock);
    var storageService = new StorageService(storageManager, clock);
    var providerService =
        new ProviderService(storageManager, metastoreService, storageService, clock);
    var principal = new Principal("Mr. Fox");
    var target = new TableService(storageManager, clock, providerService);
    createMetastore(metastoreService, principal);
    createStorage(storageService, principal);
    providerService.createProvider(
        new CreateProvider("provider1", "storage1", Optional.of("metastore1"), principal));
    assertTrue(target.getInternalTable("provider1", "icebergTable").isEmpty());
  }

  private static void createStorage(StorageService storageService, Principal principal) {
    storageService.createStorage(new CreateStorage(
        "storage1",
        Optional.empty(),
        StorageType.S3,
        principal,
        "s3://bucket",
        false,
        new StorageProperties.S3Properties(new AwsCredentials.SimpleAwsCredentials("", "", ""))));
  }

  private static void createMetastore(MetastoreService metastoreService, Principal principal) {
    metastoreService.createMetastore(new CreateMetastore(
        "metastore1",
        Optional.empty(),
        MetastoreType.GLUE,
        new MetastoreProperties.GlueMetastoreProperties(
            "", new AwsCredentials.SimpleAwsCredentials("", "", ""), MetastoreType.GLUE),
        principal,
        false));
  }
}
