package com.nexthome.collector.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HttpClientConfigurationTest {

    @Test
    void buffersLargeMonthlyTradeResponses() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        byte[] body = "x".repeat(600_000).getBytes(StandardCharsets.UTF_8);
        server.createContext("/large", exchange -> {
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        try {
            String response = new HttpClientConfiguration().webClientBuilder().build().get()
                    .uri("http://localhost:" + server.getAddress().getPort() + "/large")
                    .retrieve().bodyToMono(String.class).block();
            assertThat(response).hasSize(600_000);
        } finally {
            server.stop(0);
        }
    }
}
