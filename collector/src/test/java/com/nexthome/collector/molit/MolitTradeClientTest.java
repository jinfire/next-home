package com.nexthome.collector.molit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class MolitTradeClientTest {

    private HttpServer server;
    private String endpoint;
    private final AtomicReference<URI> requestedUri = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/getRTMSDataSvcAptTradeDev", exchange -> {
            requestedUri.set(exchange.getRequestURI());
            byte[] body = """
                    <response><header><resultCode>000</resultCode><resultMsg>OK</resultMsg></header>
                    <body><totalCount>0</totalCount><items></items></body></response>
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml;charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        endpoint = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void requestsTradesByLegalDistrictAndContractMonth() {
        MolitTradeClient client = new MolitTradeClient(
                WebClient.builder(), endpoint, "test-key", new MolitTradeXmlParser());

        MolitTradePage page = client.fetch("11110", YearMonth.of(2026, 1), 2, 50);

        assertThat(page.totalCount()).isZero();
        assertThat(requestedUri.get().getQuery())
                .contains("LAWD_CD=11110", "DEAL_YMD=202601", "pageNo=2", "numOfRows=50", "serviceKey=test-key");
    }
}
