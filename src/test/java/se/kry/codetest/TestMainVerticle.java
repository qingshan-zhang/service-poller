package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {
    //TODO: find out mock facility to mock data in both the in memory cache and database for further testing around the restApi

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    @DisplayName("Start a web server on localhost responding to path /service on port 8080")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void start_http_server(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Post request responding success")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void sendPostToServer(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJson(JsonObject.mapFrom(
                        Map.of("url", "http://www.test.com",
                                "name", "test")),
                        response -> testContext.verify(() -> {
                            assertEquals(200, response.result().statusCode());
                            testContext.completeNow();
                        }));
    }

    @Test
    @DisplayName("Post invalid Url responding bad request")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void postInvalidUrlToServer(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJson(JsonObject.mapFrom(
                        Map.of("url", "invalidUrl",
                                "name", "test")),
                        response -> testContext.verify(() -> {
                            assertEquals(400, response.result().statusCode());
                            testContext.completeNow();
                        }));
    }

    @Test
    @DisplayName("Update invalid url responding bad request")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void sendInvalidUrlUpdateToServer(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .put(8080, "::1", "/service")
                .sendJson(JsonObject.mapFrom(
                        Map.of("original_name", "test",
                                "updated_name", "test",
                                "original_url", "http://www.test.com",
                                "updated_url", "invalidUrl")),
                        response -> testContext.verify(() -> {
                            assertEquals(400, response.result().statusCode());
                            testContext.completeNow();
                        }));
    }

}
