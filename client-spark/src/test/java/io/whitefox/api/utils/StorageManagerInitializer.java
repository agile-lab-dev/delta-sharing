package io.whitefox.api.utils;

import io.whitefox.api.client.*;
import io.whitefox.api.client.model.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StorageManagerInitializer {
  private final S3TestConfig s3TestConfig;
  private final StorageV1Api storageV1Api;
  private final MetastoreV1Api metastoreV1Api;
  private final ProviderV1Api providerV1Api;
  private final TableV1Api tableV1Api;
  private final ShareV1Api shareV1Api;
  private final SchemaV1Api schemaV1Api;

  public StorageManagerInitializer() {
    var apiClient = new ApiClient();
    this.s3TestConfig = S3TestConfig.loadFromEnv();
    this.storageV1Api = new StorageV1Api(apiClient);
    this.providerV1Api = new ProviderV1Api(apiClient);
    this.tableV1Api = new TableV1Api(apiClient);
    this.shareV1Api = new ShareV1Api(apiClient);
    this.schemaV1Api = new SchemaV1Api(apiClient);
    this.metastoreV1Api = new MetastoreV1Api(apiClient);
  }

  public void initStorageManager() {
    storageV1Api.createStorage(createStorageRequest(s3TestConfig));
    shareV1Api.createShare(createShareRequest());
  }

  public void createS3DeltaTable() {
    var providerRequest = addProviderRequest(Optional.empty(), TableFormat.delta);
    providerV1Api.addProvider(providerRequest);
    var createTableRequest = createDeltaTableRequest();
    tableV1Api.createTableInProvider(providerRequest.getName(), createTableRequest);
    var shareRequest = createShareRequest();
    var schemaRequest = createSchemaRequest(TableFormat.delta);
    schemaV1Api.createSchema(shareRequest.getName(), schemaRequest);
    schemaV1Api.addTableToSchema(
        shareRequest.getName(),
        schemaRequest,
        addTableToSchemaRequest(providerRequest.getName(), createTableRequest.getName()));
  }

  public Metastore createGlueMetastore() {
    var metastoreRequest = createMetastoreRequest(s3TestConfig, CreateMetastore.TypeEnum.GLUE);
    return metastoreV1Api.createMetastore(metastoreRequest);
  }

  private String createSchemaRequest(TableFormat tableFormat) {
    return format("s3schema", tableFormat);
  }

  private AddTableToSchemaRequest addTableToSchemaRequest(String providerName, String tableName) {
    return new AddTableToSchemaRequest()
        .name(tableName)
        .reference(new TableReference().providerName(providerName).name(tableName));
  }

  private CreateShareInput createShareRequest() {
    return new CreateShareInput().name("s3share").recipients(List.of("Mr.Fox")).schemas(List.of());
  }

  private CreateTableInput createDeltaTableRequest() {
    return new CreateTableInput()
        .name("s3Table1")
        .skipValidation(true)
        .properties(Map.of(
            "type", "delta",
            "location", "s3a://whitefox-s3-test-bucket/delta/samples/delta-table"));
  }

  private ProviderInput addProviderRequest(
      Optional<String> metastoreName, TableFormat tableFormat) {
    return new ProviderInput()
        .name(format("MrFoxProvider", tableFormat))
        .storageName("MrFoxStorage")
        .metastoreName(metastoreName.orElse(null));
  }

  private CreateStorage createStorageRequest(S3TestConfig s3TestConfig) {
    return new CreateStorage()
        .name("MrFoxStorage")
        .type(CreateStorage.TypeEnum.S3)
        .properties(new StorageProperties(new S3Properties()
            .credentials(new SimpleAwsCredentials()
                .region(s3TestConfig.getRegion())
                .awsAccessKeyId(s3TestConfig.getAccessKey())
                .awsSecretAccessKey(s3TestConfig.getSecretKey()))))
        .skipValidation(true);
  }

  private CreateMetastore createMetastoreRequest(
      S3TestConfig s3TestConfig, CreateMetastore.TypeEnum type) {
    return new CreateMetastore()
        .name("MrFoxMetastore")
        .type(type)
        .skipValidation(true)
        .properties(new MetastoreProperties(new GlueProperties()
            .catalogId("catalogId") // TODO
            .credentials(new SimpleAwsCredentials()
                .region(s3TestConfig.getRegion())
                .awsAccessKeyId(s3TestConfig.getAccessKey())
                .awsSecretAccessKey(s3TestConfig.getSecretKey()))));
  }

  private String format(String value, TableFormat tableFormat) {
    return String.format("%s%s", value, tableFormat.name());
  }
}
