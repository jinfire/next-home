package com.nexthome.backend.region;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class VworldBoundaryImportJob implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(VworldBoundaryImportJob.class);
    private final VworldBoundaryGateway gateway;
    private final VworldBoundaryImportService importer;

    public VworldBoundaryImportJob(VworldBoundaryGateway gateway, VworldBoundaryImportService importer) {
        this.gateway = gateway;
        this.importer = importer;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("import-vworld-boundaries")) {
            return;
        }
        int updated = importer.importGeoJson(gateway.fetchSigungu());
        log.info("VWorld 시군구 경계 적재 완료: updated={}", updated);
    }
}
