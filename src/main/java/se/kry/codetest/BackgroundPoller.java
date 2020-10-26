package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BackgroundPoller {

  public Future<Void> pollServices(Map<String, List<ServiceStatus>> services, Vertx vertx) {

    WebClient webClient = WebClient.create(vertx);
    List<ServiceStatus> serviceStatuses = services.values()
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    serviceStatuses.stream().forEach(serviceStatus ->
      webClient.getAbs(serviceStatus.getUrl()).timeout(5000).send(response -> {
        if (response.succeeded()) {
          serviceStatus.setStatus(ServiceStatus.Status.OK);
        } else {
          serviceStatus.setStatus(ServiceStatus.Status.FAIL);
        }
      })
    );
    return Future.succeededFuture();
  }
}
