package com.nexthome.backend.region;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class VworldBoundaryGatewayTest {
    private HttpServer server;

    @AfterEach void stop() { if (server != null) server.stop(0); }

    @Test
    void requestsTheOfficialSigunguWfsLayerIn4326() throws Exception {
        AtomicReference<String> query = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/req/wfs", exchange -> {
            query.set(exchange.getRequestURI().getRawQuery());
            byte[] body = "{\"type\":\"FeatureCollection\",\"features\":[]}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        String endpoint = "http://localhost:" + server.getAddress().getPort() + "/req/wfs";

        String response = new VworldBoundaryGateway(endpoint, "test-key", "http://localhost").fetchSigungu();

        assertThat(response).contains("FeatureCollection");
        assertThat(query.get()).contains(
                "typeName=lt_c_adsigg_info",
                "output=application/json",
                "srsName=EPSG:4326",
                "bbox=33,124,39,132",
                "maxFeatures=500",
                "domain=http://localhost",
                "key=test-key");
        assertThat(query.get()).doesNotContain("outputFormat=");
    }
}
