package io.whitefox.api.deltasharing.server;

import static io.restassured.RestAssured.given;
import static io.whitefox.api.deltasharing.SampleTables.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import io.whitefox.api.deltasharing.OpenApiValidatorUtils;
import io.whitefox.api.deltasharing.S3TestConfig;
import io.whitefox.api.deltasharing.model.FileObjectWithoutPresignedUrl;
import io.whitefox.api.deltasharing.model.v1.generated.*;
import io.whitefox.core.*;
import io.whitefox.core.Share;
import io.whitefox.persistence.StorageManager;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * /**
 * Integration Tests: S3 Bucket and Delta Tables.
 *
 * These integration tests serve to validate the interaction with a dedicated test S3 bucket and Delta tables.
 * As part of the test environment, we have configured both the S3 bucket and Delta tables with
 * sample data.
 * To run the integration tests you need to:
 * 1. Obtain the required Whitefox AWS credentials:
 * - WHITEFOX_TEST_AWS_REGION
 * - WHITEFOX_TEST_AWS_ACCESS_KEY_ID
 * - WHITEFOX_TEST_AWS_SECRET_KEY
 * 2. Set these environment variables in your local environment.
 */
@QuarkusTest
@Tag("IntegrationTest")
public class ITDeltaSharesApiImplTest implements OpenApiValidatorUtils {

  @BeforeAll
  public static void setup() {
    QuarkusMock.installMockForType(storageManager, StorageManager.class);
  }

  private final ObjectMapper objectMapper;

  private final S3TestConfig s3TestConfig;

  @Inject
  public ITDeltaSharesApiImplTest(ObjectMapper objectMapper, S3TestConfig s3TestConfig) {
    this.objectMapper = objectMapper;
    this.s3TestConfig = s3TestConfig;
  }

  @BeforeEach
  public void updateStorageManagerWithS3DeltaTables() {
    storageManager.createShare(new Share(
        "s3share",
        "key",
        Map.of(
            "s3schema",
            new io.whitefox.core.Schema(
                "s3schema",
                List.of(
                    new SharedTable("s3Table1", "s3schema", "s3share", s3DeltaTable1(s3TestConfig)),
                    new SharedTable(
                        "s3table-with-history",
                        "s3schema",
                        "s3share",
                        s3DeltaTableWithHistory1(s3TestConfig))),
                "s3share")),
        new Principal("Mr fox"),
        0L));
  }

  @DisabledOnOs(OS.WINDOWS)
  @Test
  public void queryTableCurrentVersion() throws IOException {
    var responseBodyLines = given()
        .when()
        .filter(filter)
        .body("{}")
        .header(new Header("Content-Type", "application/json"))
        .post(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/query",
            "s3share",
            "s3schema",
            "s3Table1")
        .then()
        .statusCode(200)
        .extract()
        .body()
        .asString()
        .split("\n");

    assertEquals(
        s3DeltaTable1Protocol,
        objectMapper.reader().readValue(responseBodyLines[0], ProtocolObject.class));
    assertEquals(
        s3DeltaTable1Metadata,
        objectMapper.reader().readValue(responseBodyLines[1], MetadataObject.class));
    var files = Arrays.stream(responseBodyLines)
        .skip(2)
        .map(line -> {
          try {
            return objectMapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .reader()
                .readValue(line, FileObjectWithoutPresignedUrl.class);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toSet());
    assertEquals(7, responseBodyLines.length);
    assertEquals(s3DeltaTable1FilesWithoutPresignedUrl, files);
  }

  @DisabledOnOs(OS.WINDOWS)
  @Test
  public void queryTableByVersion() throws IOException {
    var responseBodyLines = given()
        .when()
        .filter(filter)
        .body("{\"version\": 0}")
        .header(new Header("Content-Type", "application/json"))
        .post(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/query",
            "s3share",
            "s3schema",
            "s3Table1")
        .then()
        .statusCode(200)
        .extract()
        .body()
        .asString()
        .split("\n");

    assertEquals(
        s3DeltaTable1Protocol,
        objectMapper.reader().readValue(responseBodyLines[0], ProtocolObject.class));
    assertEquals(
        s3DeltaTable1Metadata,
        objectMapper.reader().readValue(responseBodyLines[1], MetadataObject.class));
    var files = Arrays.stream(responseBodyLines)
        .skip(2)
        .map(line -> {
          try {
            return objectMapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .reader()
                .readValue(line, FileObjectWithoutPresignedUrl.class);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toSet());
    assertEquals(7, responseBodyLines.length);
    assertEquals(s3DeltaTable1FilesWithoutPresignedUrl, files);
  }
}
