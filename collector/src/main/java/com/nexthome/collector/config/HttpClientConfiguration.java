package com.nexthome.collector.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpClientConfiguration {

    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder().codecs(configurer ->
                configurer.defaultCodecs().maxInMemorySize(4 * 1024 * 1024));
    }
}
