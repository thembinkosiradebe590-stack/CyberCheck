package com.fileguard.service;

import com.fileguard.model.FileBaseline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Creates trusted file baselines and checks files for changes.
 */
public class BaselineService {

    private final HashService hashService;

    /**
     * Creates a service using the normal HashService.
     */
    public BaselineService() {
        this(new HashService());
    }

    /**
     * Allows a HashService to be supplied to this service.
     */
    public BaselineService(HashService hashService) {
        this.hashService = Objects.requireNonNull(
                hashService,
                "Hash service cannot be null."
        );
    }

    /**
     * Creates a trusted baseline for one file.
     */
    public FileBaseline createBaseline(
            long monitoredFolderId,
            Path file
    ) throws IOException {
        validateMonitoredFolderId(monitoredFolderId);

        String hash = hashService.calculateSHA256(file);

        return new FileBaseline(
                monitoredFolderId,
                file,
                hash,
                Files.size(file),
                Files.getLastModifiedTime(file).toInstant()
        );
    }

    /**
     * Creates trusted baselines for every file in a folder.
     */
    public List<FileBaseline> createFolderBaseline(
            long monitoredFolderId,
            Path folder
    ) throws IOException {
        validateMonitoredFolderId(monitoredFolderId);
        validateFolder(folder);

        List<FileBaseline> baselines =
                new ArrayList<>();

        try (Stream<Path> paths = Files.walk(folder)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .sorted()
                    .toList();

            for (Path file : files) {
                FileBaseline baseline =
                        createBaseline(
                                monitoredFolderId,
                                file
                        );

                baselines.add(baseline);
            }
        }

        return baselines;
    }

    /**
     * Returns true if the baseline file was modified or deleted.
     */
    public boolean hasChanged(
            FileBaseline baseline
    ) throws IOException {
        Objects.requireNonNull(
                baseline,
                "Baseline cannot be null."
        );

        Path file = baseline.getFilePath();

        if (!Files.isRegularFile(file)) {
            return true;
        }

        if (Files.size(file) != baseline.getFileSize()) {
            return true;
        }

        String currentHash =
                hashService.calculateSHA256(file);

        return !baseline.matchesHash(currentHash);
    }

    /**
     * Returns every baseline whose file changed or was deleted.
     */
    public List<FileBaseline> findChangedFiles(
            List<FileBaseline> baselines
    ) throws IOException {
        Objects.requireNonNull(
                baselines,
                "Baseline list cannot be null."
        );

        List<FileBaseline> changedFiles =
                new ArrayList<>();

        for (FileBaseline baseline : baselines) {
            if (hasChanged(baseline)) {
                changedFiles.add(baseline);
            }
        }

        return changedFiles;
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