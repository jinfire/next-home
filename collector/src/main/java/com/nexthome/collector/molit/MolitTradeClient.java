package com.nexthome.collector.molit;

import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MolitTradeClient {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    private final WebClient webClient;
    private final String serviceKey;
    private final MolitTradeXmlParser parser;

    public MolitTradeClient(
            WebClient.Builder builder,
            @Value("${next-home.molit.endpoint}") String endpoint,
            @Value("${next-home.molit.service-key}") String serviceKey,
            MolitTradeXmlParser parser) {
        this.webClient = builder.baseUrl(endpoint).build();
        this.serviceKey = serviceKey;
        this.parser = parser;
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
                .block(Duration.ofSeconds(20));
        if (xml == null || xml.isBlank()) {
            throw new MolitApiException("국토교통부 API가 빈 응답을 반환했습니다.");
        }
        return parser.parse(xml);
    }
}
