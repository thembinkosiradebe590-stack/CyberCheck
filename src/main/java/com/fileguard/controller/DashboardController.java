package com.fileguard.controller;

import com.fileguard.service.HashService;
import com.fileguard.service.ReportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DashboardController {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HashService hashService =
            new HashService();

    private final ReportService reportService =
            new ReportService();

    private final Map<Path, String> baselineHashes =
            new LinkedHashMap<>();

    private final ObservableList<String> alerts =
            FXCollections.observableArrayList();

    private Path selectedFolder;
    private boolean baselineCreated;

    @FXML
    private Label selectedFolderLabel;

    @FXML
    private Label fileCountLabel;

    @FXML
    private Label alertCountLabel;

    @FXML
    private Label lastScanLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<String> alertsListView;

    @FXML
    private void initialize() {
        selectedFolderLabel.setText("No folder selected");
        fileCountLabel.setText("0");
        alertCountLabel.setText("0");
        lastScanLabel.setText("Never");

        statusLabel.setText(
                "Select a folder to begin monitoring."
        );

        alertsListView.setItems(alerts);

        alertsListView.setPlaceholder(
                new Label(
                        "No security alerts have been detected."
                )
        );
    }

    @FXML
    private void handleSelectFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select a folder to monitor");

        File chosenFolder = chooser.showDialog(
                selectedFolderLabel
                        .getScene()
                        .getWindow()
        );

        if (chosenFolder == null) {
            return;
        }

        selectedFolder = chosenFolder
                .toPath()
                .toAbsolutePath()
                .normalize();

        baselineHashes.clear();
        alerts.clear();
        baselineCreated = false;

        selectedFolderLabel.setText(
                selectedFolder.toString()
        );

        fileCountLabel.setText("0");
        alertCountLabel.setText("0");
        lastScanLabel.setText("Never");

        statusLabel.setText(
                "Folder selected. Click Scan Folder "
                        + "to create a baseline."
        );
    }

    @FXML
    private void handleScanFolder() {
        if (selectedFolder == null) {
            statusLabel.setText(
                    "Please select a folder first."
            );
            return;
        }

        try {
            Map<Path, String> currentHashes =
                    scanFolder(selectedFolder);

            fileCountLabel.setText(
                    Integer.toString(
                            currentHashes.size()
                    )
            );

            if (!baselineCreated) {
                baselineHashes.clear();
                baselineHashes.putAll(currentHashes);
                baselineCreated = true;

                statusLabel.setText(
                        "Baseline created for "
                                + currentHashes.size()
                                + " files."
                );
            } else {
                int detectedChanges =
                        detectChanges(currentHashes);

                baselineHashes.clear();
                baselineHashes.putAll(currentHashes);

                if (detectedChanges == 0) {
                    statusLabel.setText(
                            "Scan complete. "
                                    + "No file changes detected."
                    );
                } else {
                    statusLabel.setText(
                            "Warning: "
                                    + detectedChanges
                                    + " file change(s) detected."
                    );
                }
            }

            alertCountLabel.setText(
                    Integer.toString(alerts.size())
            );

            lastScanLabel.setText(
                    LocalDateTime.now()
                            .format(TIME_FORMAT)
            );

        } catch (IOException exception) {
            statusLabel.setText(
                    "Scan failed: "
                            + exception.getMessage()
            );
        }
    }

    @FXML
    private void handleClearAlerts() {
        alerts.clear();
        alertCountLabel.setText("0");

        statusLabel.setText(
                "Security alerts cleared."
        );
    }

    @FXML
    private void handleExportReport() {
        try {
            Path reportFile =
                    reportService.generateAlertReport(
                            List.copyOf(alerts)
                    );

            statusLabel.setText(
                    "Report saved: "
                            + reportFile.getFileName()
            );

        } catch (IOException exception) {
            statusLabel.setText(
                    "Report failed: "
                            + exception.getMessage()
            );
        }
    }

    private Map<Path, String> scanFolder(Path folder)
            throws IOException {

        Map<Path, String> hashes =
                new LinkedHashMap<>();

        try (Stream<Path> paths = Files.walk(folder)) {
            for (Path file : paths
                    .filter(Files::isRegularFile)
                    .toList()) {

                Path normalizedFile = file
                        .toAbsolutePath()
                        .normalize();

                hashes.put(
                        normalizedFile,
                        hashService.calculateSHA256(
                                normalizedFile
                        )
                );
            }
        }

        return hashes;
    }

    private int detectChanges(
            Map<Path, String> currentHashes
    ) {
        int changeCount = 0;

        for (Map.Entry<Path, String> entry
                : currentHashes.entrySet()) {

            Path file = entry.getKey();
            String currentHash = entry.getValue();

            if (!baselineHashes.containsKey(file)) {
                addAlert("FILE CREATED", file);
                changeCount++;

            } else if (!baselineHashes
                    .get(file)
                    .equals(currentHash)) {

                addAlert("FILE MODIFIED", file);
                changeCount++;
            }
        }

        for (Path originalFile
                : baselineHashes.keySet()) {

            if (!currentHashes
                    .containsKey(originalFile)) {

                addAlert(
                        "FILE DELETED",
                        originalFile
                );

                changeCount++;
            }
        }

        return changeCount;
    }

    private void addAlert(
            String eventType,
            Path file
    ) {
        String time = LocalDateTime.now()
                .format(TIME_FORMAT);

        alerts.add(
                0,
                time
                        + " | "
                        + eventType
                        + " | "
                        + file
        );
    }
}