package com.nexthome.backend.region;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class VworldBoundaryGateway {
    private final RestClient client;
    private final String key;
    private final String domain;

    public VworldBoundaryGateway(
            @Value("${app.vworld.endpoint:https://api.vworld.kr/req/wfs}") String endpoint,
            @Value("${app.vworld.key:}") String key,
            @Value("${app.vworld.domain:http://localhost}") String domain) {
        this.client = RestClient.builder().baseUrl(endpoint).build();
        this.key = key;
        this.domain = domain;
    }

    public String fetchSigungu() {
        if (key.isBlank()) throw new IllegalStateException("VWORLD_API_KEY가 설정되지 않았습니다.");
        return client.get().uri(builder -> builder
                .queryParam("service", "WFS")
                .queryParam("request", "GetFeature")
                .queryParam("version", "1.1.0")
                .queryParam("typeName", "lt_c_adsigg_info")
                .queryParam("output", "application/json")
                .queryParam("srsName", "EPSG:4326")
                .queryParam("bbox", "33,124,39,132")
                .queryParam("maxFeatures", 500)
                .queryParam("domain", domain)
                .queryParam("key", key)
                .build()).retrieve().body(String.class);
    }
}
