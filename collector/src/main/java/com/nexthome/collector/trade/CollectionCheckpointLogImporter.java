package com.nexthome.collector.trade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class CollectionCheckpointLogImporter {
    private static final Pattern SUCCESS = Pattern.compile(
            "region=.*\\((\\d{5})\\), month=(\\d{4}-\\d{2}), saved=\\d+, duplicates=\\d+");
    private final CollectionCoverageStore coverage;

    public CollectionCheckpointLogImporter(CollectionCoverageStore coverage) {
        this.coverage = coverage;
    }

    public int importSuccessfulMonths(Path logPath) throws IOException {
        Set<Checkpoint> checkpoints = new LinkedHashSet<>();
        try (var lines = Files.lines(logPath)) {
            lines.forEach(line -> {
                Matcher matcher = SUCCESS.matcher(line);
                if (matcher.find()) {
                    checkpoints.add(new Checkpoint(matcher.group(1), YearMonth.parse(matcher.group(2))));
                }
            });
        }
        checkpoints.forEach(checkpoint -> coverage.markComplete(checkpoint.regionCode(), checkpoint.month()));
        return checkpoints.size();
    }

    private record Checkpoint(String regionCode, YearMonth month) {
    }
}
