package com.nexthome.collector.trade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
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

    @Test
    void readsWindowsConsoleLogsEvenWhenTheyAreNotValidUtf8() throws Exception {
        Path log = tempDir.resolve("windows-console.log");
        byte[] success = " region=x(41450), month=2021-02, saved=1, duplicates=0\n"
                .getBytes(StandardCharsets.US_ASCII);
        byte[] bytes = new byte[success.length + 1];
        bytes[0] = (byte) 0xFF;
        System.arraycopy(success, 0, bytes, 1, success.length);
        Files.write(log, bytes);
        CollectionCoverageStore coverage = mock(CollectionCoverageStore.class);

        int imported = new CollectionCheckpointLogImporter(coverage).importSuccessfulMonths(log);

        assertThat(imported).isEqualTo(1);
        verify(coverage).markComplete("41450", YearMonth.of(2021, 2));
    }
}
