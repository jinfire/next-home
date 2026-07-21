package com.nexthome.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public final class CapitalAreaTradeCollectionApplication {
    private CapitalAreaTradeCollectionApplication() {
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(CollectorApplication.class)
                .web(WebApplicationType.NONE)
                .run("--next-home.capital-collector.enabled=true");
        System.exit(SpringApplication.exit(context));
    }
}
