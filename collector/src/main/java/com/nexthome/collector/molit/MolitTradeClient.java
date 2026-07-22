package com.nexthome.collector.molit;

import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

@Component
public class MolitTradeClient {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    private final WebClient webClient;
    private final String serviceKey;
    private final MolitTradeXmlParser parser;
    private final int maxRetries;
    private final Duration retryBackoff;

    @Autowired
    public MolitTradeClient(
            WebClient.Builder builder,
            @Value("${next-home.molit.endpoint}") String endpoint,
            @Value("${next-home.molit.service-key}") String serviceKey,
            MolitTradeXmlParser parser,
            @Value("${next-home.molit.max-retries:4}") int maxRetries,
            @Value("${next-home.molit.retry-backoff:2s}") Duration retryBackoff) {
        this.webClient = builder.baseUrl(endpoint).build();
        this.serviceKey = serviceKey;
        this.parser = parser;
        this.maxRetries = maxRetries;
        this.retryBackoff = retryBackoff;
    }

    MolitTradeClient(WebClient.Builder builder, String endpoint, String serviceKey, MolitTradeXmlParser parser) {
        this(builder, endpoint, serviceKey, parser, 4, Duration.ofSeconds(2));
    }

    public MolitTradePage fetch(String legalDistrictCode, YearMonth contractMonth, int page, int rows) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new MolitApiException("MOLIT_API_KEY가 설정되지 않았습니다.");
        }
        String xml = webClient.get()
                .uri(uri -> uri.path("/getRTMSDataSvcAptTradeDev")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("LAWD_CD", legalDistrictCode)
                        .queryParam("DEAL_YMD", contractMonth.format(MONTH_FORMAT))
                        .queryParam("pageNo", page)
                        .queryParam("numOfRows", rows)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(20))
                .retryWhen(Retry.backoff(maxRetries, retryBackoff)
                        .maxBackoff(Duration.ofMinutes(1))
                        .filter(this::isRetryable)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                .block(Duration.ofMinutes(5));
        if (xml == null || xml.isBlank()) {
            throw new MolitApiException("국토교통부 API가 빈 응답을 반환했습니다.");
        }
        return parser.parse(xml);
    }

    private boolean isRetryable(Throwable error) {
        if (error instanceof WebClientRequestException || error instanceof java.util.concurrent.TimeoutException) {
            return true;
        }
        if (!(error instanceof WebClientResponseException response)) return false;
        return response.getStatusCode().value() == 429 || response.getStatusCode().is5xxServerError();
    }
}
