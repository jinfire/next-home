package com.nexthome.backend.region;

import com.nexthome.backend.BackendApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public final class VworldBoundaryImportApplication {
    private VworldBoundaryImportApplication() {
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(BackendApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.task.scheduling.enabled=false")
                .run("--import-vworld-boundaries");
        System.exit(SpringApplication.exit(context));
    }
}
