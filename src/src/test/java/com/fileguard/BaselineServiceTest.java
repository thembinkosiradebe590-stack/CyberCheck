package com.fileguard;

import com.fileguard.model.FileBaseline;
import com.fileguard.service.BaselineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaselineServiceTest {

    private static final String HELLO_HASH =
            "2cf24dba5fb0a30e26e83b2ac5b9e29"
                    + "e1b161e5c1fa7425e73043362938b9824";

    private final BaselineService baselineService = new BaselineService();

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsBaselineForFile() throws Exception {
        Path file = temporaryDirectory.resolve("hello.txt");
        Files.writeString(file, "hello");

        FileBaseline baseline = baselineService.createBaseline(1, file);

        assertEquals(file.toAbsolutePath().normalize(), baseline.getFilePath());
        assertEquals(HELLO_HASH, baseline.getSha256Hash());
        assertEquals(5, baseline.getFileSize());
        assertEquals(1, baseline.getMonitoredFolderId());
    }

    @Test
    void createsBaselinesForFolderFiles() throws Exception {
        Path nestedFolder = temporaryDirectory.resolve("documents");
        Files.createDirectories(nestedFolder);
        Files.writeString(temporaryDirectory.resolve("first.txt"), "first");
        Files.writeString(nestedFolder.resolve("second.txt"), "second");

        List<FileBaseline> baselines = baselineService.createFolderBaseline(
                1,
                temporaryDirectory
        );

        assertEquals(2, baselines.size());
    }

    @Test
    void reportsUnchangedFileAsSafe() throws Exception {
        Path file = temporaryDirectory.resolve("safe.txt");
        Files.writeString(file, "original");

        FileBaseline baseline = baselineService.createBaseline(1, file);

        assertFalse(baselineService.hasChanged(baseline));
    }

    @Test
    void detectsModifiedFileWithSameSize() throws Exception {
        Path file = temporaryDirectory.resolve("important.txt");
        Files.writeString(file, "original");

        FileBaseline baseline = baselineService.createBaseline(1, file);
        Files.writeString(file, "modified");

        assertTrue(baselineService.hasChanged(baseline));
    }

    @Test
    void detectsDeletedFile() throws Exception {
        Path file = temporaryDirectory.resolve("delete-me.txt");
        Files.writeString(file, "important data");

        FileBaseline baseline = baselineService.createBaseline(1, file);
        Files.delete(file);

        assertTrue(baselineService.hasChanged(baseline));
    }

    @Test
    void returnsOnlyChangedFiles() throws Exception {
        Path safeFile = temporaryDirectory.resolve("safe.txt");
        Path changedFile = temporaryDirectory.resolve("changed.txt");
        Files.writeString(safeFile, "safe");
        Files.writeString(changedFile, "before");

        FileBaseline safeBaseline = baselineService.createBaseline(1, safeFile);
        FileBaseline changedBaseline = baselineService.createBaseline(
                1,
                changedFile
        );

        Files.writeString(changedFile, "after!");

        List<FileBaseline> changedFiles = baselineService.findChangedFiles(
                List.of(safeBaseline, changedBaseline)
        );

        assertEquals(1, changedFiles.size());
        assertEquals(
                changedFile.toAbsolutePath().normalize(),
                changedFiles.get(0).getFilePath()
        );
    }
}
