package io.whitefox.api.deltasharing.server;

import io.quarkus.runtime.util.ExceptionUtil;
import io.whitefox.api.deltasharing.DeltaSharedTable;
import io.whitefox.api.deltasharing.loader.DeltaShareTableLoader;
import io.whitefox.api.deltasharing.model.*;
import io.whitefox.services.ContentAndToken;
import io.whitefox.services.DeltaSharesService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class DeltaSharesApiImpl implements DeltaApiApi {

  private final DeltaSharesService deltaSharesService;
  private static final String DELTA_TABLE_VERSION_HEADER = "Delta-Table-Version";
  private static final Function<Throwable, Response> exceptionToResponse =
      t -> Response.status(Response.Status.BAD_GATEWAY)
          .entity(new CommonErrorResponse()
              .errorCode("BAD GATEWAY")
              .message(ExceptionUtil.generateStackTrace(t)))
          .build();


  private Response notFoundResponse() {
    return Response.status(Response.Status.NOT_FOUND)
            .entity(new CommonErrorResponse().errorCode("1").message("NOT FOUND"))
            .build();
  }

  private <T> CompletionStage<Response> optionalToNotFoundAsync(
  Optional<T> opt, Function<T, CompletionStage<Response>> fn) {
    return opt.map(fn)
        .orElse(CompletableFuture.supplyAsync(this::notFoundResponse))
            .exceptionally(exceptionToResponse);
  }

  private <T> Response optionalToNotFound(Optional<T> opt, Function<T, Response> fn) {
    return opt.map(fn).orElse(notFoundResponse());
  }

  @Inject
  public DeltaSharesApiImpl(DeltaSharesService deltaSharesService) {
    this.deltaSharesService = deltaSharesService;
  }

  @Override
  public CompletionStage<Response> getShare(String share) {
    return deltaSharesService
        .getShare(share)
        .thenApplyAsync(o -> optionalToNotFound(o, s -> Response.ok(s).build()))
        .exceptionallyAsync(exceptionToResponse);
  }

  @Override
  public CompletionStage<Response> getTableChanges(
      String share,
      String schema,
      String table,
      String startingTimestamp,
      Integer startingVersion,
      Integer endingVersion,
      String endingTimestamp,
      Boolean includeHistoricalMetadata) {
    Response res = Response.ok().build();
    return CompletableFuture.completedFuture(res);
  }

  @Override
  public CompletionStage<Response> getTableMetadata(
      String share, String schema, String table, String startingTimestamp) {
    Response res = Response.ok().build();
    return CompletableFuture.completedFuture(res);
  }

  @Override
  public CompletionStage<Response> getTableVersion(
      String share, String schema, String table, String startingTimestamp) {
    return deltaSharesService
            .getTable(share, schema, table)
            .thenCompose(o -> optionalToNotFoundAsync(o,
                    pTable -> new DeltaShareTableLoader()
                            .loadTable(pTable)
                            .getTableVersion(Optional.ofNullable(startingTimestamp))
                            .thenApply(t -> Response.ok()
                                    .status(Response.Status.OK.getStatusCode())
                                    .header(DELTA_TABLE_VERSION_HEADER, t)
                                    .build())
                            .exceptionally(exceptionToResponse))
                    ).exceptionally(exceptionToResponse);
  }

  @Override
  public CompletionStage<Response> listALLTables(
      String share, Integer maxResults, String pageToken) {
    return deltaSharesService
        .listTablesOfShare(
            share,
            Optional.ofNullable(pageToken).map(ContentAndToken.Token::new),
            Optional.ofNullable(maxResults))
        .toCompletableFuture()
        .thenApplyAsync(o -> optionalToNotFound(o, c -> Response.ok(c.getToken()
                .map(t -> new ListTablesResponse().items(c.getContent()).nextPageToken(t))
                .orElse(new ListTablesResponse().items(c.getContent())))
            .build()))
        .exceptionallyAsync(exceptionToResponse);
  }

  @Override
  public CompletionStage<Response> listSchemas(String share, Integer maxResults, String pageToken) {
    var shares = deltaSharesService.listSchemas(
        share,
        Optional.ofNullable(pageToken).map(ContentAndToken.Token::new),
        Optional.ofNullable(maxResults));
    return shares
        .thenApplyAsync(o -> optionalToNotFound(o, ct -> Response.ok(ct.getToken()
                .map(t -> new ListSchemasResponse().nextPageToken(t).items(ct.getContent()))
                .orElse(new ListSchemasResponse().items(ct.getContent())))
            .build()))
        .exceptionallyAsync(exceptionToResponse);
  }

  @Override
  public CompletionStage<Response> listShares(Integer maxResults, String pageToken) {
    return deltaSharesService
        .listShares(
            Optional.ofNullable(pageToken).map(ContentAndToken.Token::new),
            Optional.ofNullable(maxResults))
        .toCompletableFuture()
        .thenApplyAsync(c -> Response.ok(c.getToken()
                .map(t -> new ListShareResponse().items(c.getContent()).nextPageToken(t))
                .orElse(new ListShareResponse().items(c.getContent())))
            .build())
        .exceptionallyAsync(exceptionToResponse);
  }

  @Override
  public CompletionStage<Response> listTables(
      String share, String schema, Integer maxResults, String pageToken) {
    return deltaSharesService
        .listTables(
            share,
            schema,
            Optional.ofNullable(pageToken).map(ContentAndToken.Token::new),
            Optional.ofNullable(maxResults))
        .toCompletableFuture()
        .thenApplyAsync(o -> optionalToNotFound(o, c -> Response.ok(c.getToken()
                .map(t -> new ListTablesResponse().items(c.getContent()).nextPageToken(t))
                .orElse(new ListTablesResponse().items(c.getContent())))
            .build()))
        .exceptionallyAsync(exceptionToResponse);
  }

  @Override
  public CompletionStage<Response> queryTable(
      String share,
      String schema,
      String table,
      QueryRequest queryRequest,
      String startingTimestamp) {
    Response res = Response.ok().build();
    return CompletableFuture.completedFuture(res);
  }
}
