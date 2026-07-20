package com.nexthome.backend.geocoding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class NaverGeocodingGateway implements GeocodingGateway {
    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String clientId;
    private final String clientSecret;

    NaverGeocodingGateway(
            @Value("${app.naver.geocoding.endpoint:https://maps.apigw.ntruss.com/map-geocode/v2/geocode}") String endpoint,
            @Value("${app.naver.client-id:}") String clientId,
            @Value("${app.naver.client-secret:}") String clientSecret) {
        this.client = RestClient.builder().baseUrl(endpoint).build();
        this.objectMapper = new ObjectMapper();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public Optional<GeocodingResult> geocode(String normalizedAddress) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException("NAVER Maps credentials are not configured");
        }
        String body = client.get()
                .uri(uri -> uri.queryParam("query", normalizedAddress).build())
                .header("x-ncp-apigw-api-key-id", clientId)
                .header("x-ncp-apigw-api-key", clientSecret)
                .retrieve()
                .body(String.class);
        try {
            JsonNode first = objectMapper.readTree(body).path("addresses").path(0);
            if (first.isMissingNode()) return Optional.empty();
            String roadAddress = first.path("roadAddress").asText(normalizedAddress);
            return Optional.of(new GeocodingResult(normalizedAddress, roadAddress,
                    new BigDecimal(first.path("x").asText()),
                    new BigDecimal(first.path("y").asText())));
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid NAVER Geocoding response", exception);
        }
    }
}
