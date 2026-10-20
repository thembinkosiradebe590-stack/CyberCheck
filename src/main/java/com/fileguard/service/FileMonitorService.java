package com.fileguard.service;

import com.fileguard.model.EventType;
import com.fileguard.model.FileBaseline;
import com.fileguard.model.SecurityEvent;
import com.fileguard.model.Severity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Scans a folder and creates security events for file changes.
 */
public class FileMonitorService {

    private final BaselineService baselineService;

    /**
     * Creates a monitor using the normal BaselineService.
     */
    public FileMonitorService() {
        this(new BaselineService());
    }

    /**
     * Allows a BaselineService to be supplied for testing.
     */
    public FileMonitorService(
            BaselineService baselineService
    ) {
        this.baselineService = Objects.requireNonNull(
                baselineService,
                "Baseline service cannot be null."
        );
    }

    /**
     * Scans a folder and detects created, modified,
     * and deleted files.
     */
    public List<SecurityEvent> scanFolder(
            long monitoredFolderId,
            Path folder,
            List<FileBaseline> baselines
    ) throws IOException {
        validateMonitoredFolderId(monitoredFolderId);
        validateFolder(folder);

        Objects.requireNonNull(
                baselines,
                "Baseline list cannot be null."
        );

        Map<Path, FileBaseline> baselineByPath =
                createBaselineMap(baselines);

        List<Path> currentFiles =
                findCurrentFiles(folder);

        List<SecurityEvent> events =
                new ArrayList<>();

        detectModifiedAndDeletedFiles(
                monitoredFolderId,
                baselineByPath,
                currentFiles,
                events
        );

        detectCreatedFiles(
                monitoredFolderId,
                baselineByPath,
                currentFiles,
                events
        );

        return events;
    }

    private Map<Path, FileBaseline> createBaselineMap(
            List<FileBaseline> baselines
    ) {
        Map<Path, FileBaseline> baselineByPath =
                new HashMap<>();

        for (FileBaseline baseline : baselines) {
            Objects.requireNonNull(
                    baseline,
                    "Baseline list cannot contain null."
            );

            Path normalizedPath =
                    normalize(baseline.getFilePath());

            baselineByPath.put(
                    normalizedPath,
                    baseline
            );
        }

        return baselineByPath;
    }

    private List<Path> findCurrentFiles(
            Path folder
    ) throws IOException {
        try (Stream<Path> paths = Files.walk(folder)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(this::normalize)
                    .sorted()
                    .toList();
        }
    }

    private void detectModifiedAndDeletedFiles(
            long monitoredFolderId,
            Map<Path, FileBaseline> baselineByPath,
            List<Path> currentFiles,
            List<SecurityEvent> events
    ) throws IOException {
        for (Map.Entry<Path, FileBaseline> entry
                : baselineByPath.entrySet()) {

            Path file = entry.getKey();
            FileBaseline baseline = entry.getValue();

            if (!currentFiles.contains(file)) {
                events.add(
                        createDeletedEvent(
                                monitoredFolderId,
                                file
                        )
                );
            } else if (baselineService.hasChanged(baseline)) {
                events.add(
                        createModifiedEvent(
                                monitoredFolderId,
                                file
                        )
                );
            }
        }
    }

    private void detectCreatedFiles(
            long monitoredFolderId,
            Map<Path, FileBaseline> baselineByPath,
            List<Path> currentFiles,
            List<SecurityEvent> events
    ) {
        for (Path file : currentFiles) {
            if (!baselineByPath.containsKey(file)) {
                events.add(
                        createCreatedEvent(
                                monitoredFolderId,
                                file
                        )
                );
            }
        }
    }

    private SecurityEvent createCreatedEvent(
            long monitoredFolderId,
            Path file
    ) {
        return new SecurityEvent(
                monitoredFolderId,
                file,
                EventType.FILE_CREATED,
                Severity.LOW,
                "A new file was created."
        );
    }

    private SecurityEvent createModifiedEvent(
            long monitoredFolderId,
            Path file
    ) {
        return new SecurityEvent(
                monitoredFolderId,
                file,
                EventType.FILE_MODIFIED,
                Severity.MEDIUM,
                "A monitored file was modified."
        );
    }

    private SecurityEvent createDeletedEvent(
            long monitoredFolderId,
            Path file
    ) {
        return new SecurityEvent(
                monitoredFolderId,
                file,
                EventType.FILE_DELETED,
                Severity.HIGH,
                "A monitored file was deleted."
        );
    }

    private Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private void validateMonitoredFolderId(
            long monitoredFolderId
    ) {
        if (monitoredFolderId < 0) {
            throw new IllegalArgumentException(
                    "The monitored folder ID cannot be negative."
            );
        }
    }

    private void validateFolder(Path folder) {
        if (folder == null) {
            throw new IllegalArgumentException(
                    "Folder path cannot be null."
            );
        }

        if (!Files.exists(folder)) {
            throw new IllegalArgumentException(
                    "The selected folder does not exist."
            );
        }

        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException(
                    "The selected path is not a folder."
            );
        }

        if (!Files.isReadable(folder)) {
            throw new IllegalArgumentException(
                    "The selected folder cannot be read."
            );
        }
    }
}