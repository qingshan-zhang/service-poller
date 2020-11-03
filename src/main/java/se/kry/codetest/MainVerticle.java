package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

    private HashMap<String, List<ServiceStatus>> userServices = new HashMap<>();
    private DBConnector connector;
    private BackgroundPoller poller = new BackgroundPoller();

    @Override
    public void start(Future<Void> startFuture) {
        connector = new DBConnector(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        vertx.setPeriodic(1000 * 10, timerId -> poller.pollServices(userServices, vertx));
        setRoutes(router);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private void setRoutes(Router router) {
        router.route("/*")
                .handler(StaticHandler.create().setCachingEnabled(false))
                .handler(CookieHandler.create());

        router.get("/service").handler(req -> {
            Cookie cookie = req.getCookie("kry");
            String user = getUserOrDefault(cookie);

            List<ServiceStatus> servicesStatus = userServices.get(user);
            if (servicesStatus == null || servicesStatus.isEmpty()) {
                connector.query("select url, name from service where user = ?", new JsonArray().add(user))
                        .setHandler(
                                response -> {
                                    if (response.succeeded()) {
                                        ResultSet resultSet = response.result();
                                        List<JsonObject> rows = resultSet.getRows();
                                        List<ServiceStatus> services = rows.stream().map(row -> new ServiceStatus(row.getString("url"), row.getString("name"))).collect(Collectors.toList());
                                        userServices.put(user, services);
                                        List<JsonObject> status = getServicesStatus(userServices.get(user));
                                        req.response()
                                                .putHeader("content-type", "application/json")
                                                .end(new JsonArray(status).encode());
                                    } else {
                                        req.fail(500);
                                    }
                                }
                        );
            } else {
                List<JsonObject> jsonServices = getServicesStatus(servicesStatus);
                req.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonArray(jsonServices).encode());
            }
        });

        router.post("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();
            String url = jsonBody.getString("url");
            String name = jsonBody.getString("name");
            Cookie cookie = req.getCookie("kry");
            String user = getUserOrDefault(cookie);

            try {
                URL wrappedUrl = new URL(url);

                List<ServiceStatus> services = userServices.get(user);
                if (services == null || services.isEmpty()) {
                    services = new ArrayList<>();
                }
                services.add(new ServiceStatus(url, name));
                userServices.put(user, services);
                upsertUrlRecord(url, name, user).setHandler(result -> {
                    if (result.succeeded()) {
                        req.response()
                                .putHeader("content-type", "text/plain")
                                .end("OK");
                    } else {
                        req.fail(500);
                    }
                });
            } catch (MalformedURLException e) {
                req.fail(400);
            }
        });

        router.put("/service").handler(req-> {
           JsonObject jsonBody = req.getBodyAsJson();
           String originalName = jsonBody.getString("original_name");
           String updatedName = jsonBody.getString("updated_name");
           String originalUrl = jsonBody.getString("original_url");
           String updatedUrl = jsonBody.getString("updated_url");
           Cookie cookie = req.getCookie("kry");
           String user = getUserOrDefault(cookie);
           List<ServiceStatus> serviceStatuses = userServices.get(user);
            ServiceStatus serviceStatus = serviceStatuses.stream()
                    .filter(service -> service.getUrl().equals(originalUrl)
                            && service.getName().equals(originalName))
                    .findFirst()
                    .get();
            serviceStatus.setName(updatedName);
            serviceStatus.setUrl(updatedUrl);
            updateUrlRecord(originalName, originalUrl, updatedName, updatedUrl, user);
        });


        router.delete("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();
            String url = jsonBody.getString("url");
            Cookie cookie = req.getCookie("kry");
            String user = getUserOrDefault(cookie);
            List<ServiceStatus> serviceStatuses = userServices.get(user);
            ServiceStatus toDelete = serviceStatuses.stream()
                    .filter(service -> service.getUrl().equals(url))
                    .findFirst().get();
            serviceStatuses.remove(toDelete);
            deleteUrlRecord(url, user).setHandler(result -> {
                if (result.succeeded()) {
                    req.response()
                            .putHeader("content-type", "text/plain")
                            .end("OK");
                } else {
                    req.fail(500);
                }
            });
        });
    }

    private String getUserOrDefault(Cookie cookie) {
        return cookie == null ? "" : cookie.getValue();
    }

    private List<JsonObject> getServicesStatus(List<ServiceStatus> services) {
        return services
                .stream()
                .map(service -> new JsonObject()
                        .put("url", service.getUrl())
                        .put("name", service.getName())
                        .put("status", service.getStatus()))
                .collect(Collectors.toList());
    }

    private Future<Void> upsertUrlRecord(String url, String name, String user) {
        String updateSql = "insert into service (url, name, user, timestamp) values (?,?,?, datetime('now'));";
        return connector.update(updateSql, new JsonArray().add(url).add(name).add(user));
    }

    private Future<Void> deleteUrlRecord(String url, String user) {
        String deleteSql = "delete from service where url = ? and user = ?;";
        return connector.update(deleteSql, new JsonArray().add(url).add(user));
    }

    private Future<Void> updateUrlRecord(String originalName, String originalUrl, String updatedName, String updatedUrl, String user) {
        String updateSql = "update service set url = ?, name = ? where name = ? and url = ? and user = ?";
        return connector.update(updateSql, new JsonArray().add(updatedUrl).add(updatedName).add(originalName).add(originalUrl).add(user));
    }

}



