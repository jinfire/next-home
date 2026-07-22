package com.nexthome.collector.trade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CollectionCheckpointLogImporterTest {

    @TempDir
    Path tempDir;

    @Test
    void restoresUniqueSuccessfulRegionMonthsFromAnExistingCollectorLog() throws Exception {
        Path log = tempDir.resolve("history.log");
        Files.writeString(log, """
                INFO 수도권 실거래 수집: region=종로구(11110), month=2015-01, saved=10, duplicates=0
                INFO 수도권 실거래 수집: region=종로구(11110), month=2015-01, saved=0, duplicates=10
                ERROR region=중구(11140), month=2015-02
                INFO 수도권 실거래 수집: region=중구(11140), month=2015-03, saved=20, duplicates=0
                """);
        CollectionCoverageStore coverage = mock(CollectionCoverageStore.class);

        int imported = new CollectionCheckpointLogImporter(coverage).importSuccessfulMonths(log);

        assertThat(imported).isEqualTo(2);
        verify(coverage).markComplete("11110", YearMonth.of(2015, 1));
        verify(coverage).markComplete("11140", YearMonth.of(2015, 3));
    }
}
