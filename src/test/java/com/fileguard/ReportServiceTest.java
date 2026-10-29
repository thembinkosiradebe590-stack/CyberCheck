package com.fileguard;

import com.fileguard.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {

    private final ReportService reportService =
            new ReportService();

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsSecurityReportFile() throws Exception {
        Path report = reportService.generateAlertReport(
                List.of(
                        "2026-10-29 10:00:00 | FILE MODIFIED | important.txt"
                ),
                temporaryDirectory
        );

        assertTrue(Files.exists(report));
        assertTrue(Files.isRegularFile(report));
        assertTrue(
                report.getFileName()
                        .toString()
                        .startsWith("fileguard-security-report-")
        );
    }

    @Test
    void reportContainsAlertCountAndEvents() throws Exception {
        List<String> alerts = List.of(
                "2026-10-29 10:00:00 | FILE CREATED | new.txt",
                "2026-10-29 10:01:00 | FILE DELETED | old.txt"
        );

        Path report = reportService.generateAlertReport(
                alerts,
                temporaryDirectory
        );

        String content = Files.readString(report);

        assertTrue(
                content.contains("FILEGUARD SECURITY REPORT")
        );

        assertTrue(
                content.contains("Total security alerts: 2")
        );

        assertTrue(
                content.contains("FILE CREATED | new.txt")
        );

        assertTrue(
                content.contains("FILE DELETED | old.txt")
        );
    }

    @Test
    void reportHandlesEmptyAlertList() throws Exception {
        Path report = reportService.generateAlertReport(
                List.of(),
                temporaryDirectory
        );

        String content = Files.readString(report);

        assertTrue(
                content.contains("Total security alerts: 0")
        );

        assertTrue(
                content.contains(
                        "No security alerts were detected."
                )
        );
    }

    @Test
    void rejectsNullAlertList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reportService.generateAlertReport(
                        null,
                        temporaryDirectory
                )
        );
    }
}