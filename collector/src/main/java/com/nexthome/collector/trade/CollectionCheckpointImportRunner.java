package com.nexthome.collector.trade;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "next-home.checkpoint-import.enabled", havingValue = "true")
public class CollectionCheckpointImportRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CollectionCheckpointImportRunner.class);
    private final CollectionCheckpointLogImporter importer;
    private final Path logPath;

    public CollectionCheckpointImportRunner(CollectionCheckpointLogImporter importer,
            @Value("${next-home.checkpoint-import.log-path}") String logPath) {
        this.importer = importer;
        this.logPath = Path.of(logPath);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int imported = importer.importSuccessfulMonths(logPath);
        log.info("수집 로그 체크포인트 복구 완료: log={}, regionMonths={}", logPath, imported);
    }
}
