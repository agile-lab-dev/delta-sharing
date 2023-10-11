package io.whitefox.api.deltasharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.wildfly.common.Assert.assertTrue;

import io.whitefox.core.Table;
import io.whitefox.core.services.DeltaSharedTable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

// @DisabledOnOs(OS.WINDOWS)
public class DeltaSharedTableTest {

  private static final Path deltaTablesRoot = Paths.get(".")
      .toAbsolutePath()
      .resolve("src/test/resources/delta/samples")
      .toAbsolutePath();

  private static String tablePath(String tableName) {
    return deltaTablesRoot.resolve(tableName).toUri().toString();
  }

  @Test
  void getTableVersion() throws ExecutionException, InterruptedException {
    var PTable = new Table("delta-table", tablePath("delta-table"), "default", "share1");
    var DTable = DeltaSharedTable.of(PTable);
    var version = DTable.getTableVersion(Optional.empty());
    assertEquals(Optional.of(0L), version);
  }

  @Test
  void getTableVersionNonExistingTable() throws ExecutionException, InterruptedException {
    var PTable = new Table("delta-table", tablePath("delta-table-not-exists"), "default", "share1");
    var exception = assertThrows(IllegalArgumentException.class, () -> DeltaSharedTable.of(PTable));
    assertTrue(exception.getMessage().startsWith("Cannot find a delta table at file"));
  }

  @Test
  void getTableVersionWithTimestamp() throws ExecutionException, InterruptedException {
    var PTable = new Table("delta-table", tablePath("delta-table"), "default", "share1");
    var DTable = DeltaSharedTable.of(PTable);
    var version = DTable.getTableVersion(Optional.of("2023-09-30T10:15:30+01:00"));
    assertEquals(Optional.empty(), version);
  }

  @Test
  void getTableVersionWithFutureTimestamp() throws ExecutionException, InterruptedException {
    var PTable = new Table("delta-table", tablePath("delta-table"), "default", "share1");
    var DTable = DeltaSharedTable.of(PTable);
    var version = DTable.getTableVersion(Optional.of("2024-10-20T10:15:30+01:00"));
    assertEquals(Optional.empty(), version);
  }

  @Test
  void getTableVersionWithMalformedTimestamp() throws ExecutionException, InterruptedException {
    var PTable = new Table("delta-table", tablePath("delta-table"), "default", "share1");
    var DTable = DeltaSharedTable.of(PTable);
    assertThrows(
        DateTimeParseException.class,
        () -> DTable.getTableVersion(Optional.of("221rfewdsad10:15:30+01:00")));
  }
}
