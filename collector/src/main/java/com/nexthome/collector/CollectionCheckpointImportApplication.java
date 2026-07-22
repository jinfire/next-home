package com.nexthome.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public final class CollectionCheckpointImportApplication {
    private CollectionCheckpointImportApplication() {
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(CollectorApplication.class)
                .web(WebApplicationType.NONE)
                .run("--next-home.checkpoint-import.enabled=true");
        System.exit(SpringApplication.exit(context));
    }
}
