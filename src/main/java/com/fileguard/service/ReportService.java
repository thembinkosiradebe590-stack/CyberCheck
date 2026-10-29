package com.fileguard.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {

    private static final DateTimeFormatter FILE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private static final DateTimeFormatter REPORT_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Path generateAlertReport(List<String> alerts)
            throws IOException {

        Path defaultDirectory = Path.of(
                "reports",
                "generated-reports"
        );

        return generateAlertReport(alerts, defaultDirectory);
    }

    public Path generateAlertReport(
            List<String> alerts,
            Path outputDirectory
    ) throws IOException {

        validateInput(alerts, outputDirectory);

        Files.createDirectories(outputDirectory);

        LocalDateTime generatedAt = LocalDateTime.now();

        String fileName = "fileguard-security-report-"
                + generatedAt.format(FILE_TIME_FORMAT)
                + ".txt";

        Path reportFile = outputDirectory
                .resolve(fileName)
                .toAbsolutePath()
                .normalize();

        String reportContent = buildReport(alerts, generatedAt);

        Files.writeString(
                reportFile,
                reportContent,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW
        );

        return reportFile;
    }

    private String buildReport(
            List<String> alerts,
            LocalDateTime generatedAt
    ) {

        StringBuilder report = new StringBuilder();

        report.append("FILEGUARD SECURITY REPORT")
                .append(System.lineSeparator());

        report.append("=========================")
                .append(System.lineSeparator());

        report.append("Generated: ")
                .append(generatedAt.format(REPORT_TIME_FORMAT))
                .append(System.lineSeparator());

        report.append("Total security alerts: ")
                .append(alerts.size())
                .append(System.lineSeparator());

        report.append(System.lineSeparator());

        report.append("SECURITY EVENTS")
                .append(System.lineSeparator());

        report.append("-------------------------")
                .append(System.lineSeparator());

        if (alerts.isEmpty()) {
            report.append("No security alerts were detected.")
                    .append(System.lineSeparator());
        } else {
            for (int index = 0; index < alerts.size(); index++) {
                report.append(index + 1)
                        .append(". ")
                        .append(alerts.get(index))
                        .append(System.lineSeparator());
            }
        }

        report.append(System.lineSeparator());

        report.append("End of FileGuard report.")
                .append(System.lineSeparator());

        return report.toString();
    }

    private void validateInput(
            List<String> alerts,
            Path outputDirectory
    ) {

        if (alerts == null) {
            throw new IllegalArgumentException(
                    "The alerts list cannot be null."
            );
        }

        if (outputDirectory == null) {
            throw new IllegalArgumentException(
                    "The output directory cannot be null."
            );
        }

        for (String alert : alerts) {
            if (alert == null || alert.isBlank()) {
                throw new IllegalArgumentException(
                        "Security alert entries cannot be blank."
                );
            }
        }
    }
}