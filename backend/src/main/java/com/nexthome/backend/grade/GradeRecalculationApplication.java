package com.nexthome.backend.grade;

import com.nexthome.backend.BackendApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public final class GradeRecalculationApplication {
    private GradeRecalculationApplication() {
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(BackendApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.task.scheduling.enabled=false")
                .run("--recalculate-grades");
        System.exit(SpringApplication.exit(context));
    }
}
