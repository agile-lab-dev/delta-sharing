package io.delta.sharing.api.server;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class DeltaSharesApiImplTest {

    @Test
    public void getUnknownShare() {
        given()
                .pathParam("share", "unknownKey")
                .when()
                .get("/share/{share}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

}
