package com.fileguard;

import com.fileguard.model.EventType;
import com.fileguard.model.FileBaseline;
import com.fileguard.model.SecurityEvent;
import com.fileguard.model.Severity;
import com.fileguard.service.BaselineService;
import com.fileguard.service.FileMonitorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileMonitorServiceTest {

    private final BaselineService baselineService =
            new BaselineService();

    private final FileMonitorService monitorService =
            new FileMonitorService(baselineService);

    @TempDir
    Path temporaryDirectory;

    @Test
    void returnsNoEventsWhenFilesAreUnchanged()
            throws Exception {
        Path file =
                temporaryDirectory.resolve("safe.txt");

        Files.writeString(file, "safe content");

        List<FileBaseline> baselines =
                baselineService.createFolderBaseline(
                        1,
                        temporaryDirectory
                );

        List<SecurityEvent> events =
                monitorService.scanFolder(
                        1,
                        temporaryDirectory,
                        baselines
                );

        assertTrue(events.isEmpty());
    }

    @Test
    void detectsCreatedFile() throws Exception {
        List<FileBaseline> baselines =
                baselineService.createFolderBaseline(
                        1,
                        temporaryDirectory
                );

        Path newFile =
                temporaryDirectory.resolve("new.txt");

        Files.writeString(newFile, "new content");

        List<SecurityEvent> events =
                monitorService.scanFolder(
                        1,
                        temporaryDirectory,
                        baselines
                );

        assertEquals(1, events.size());
        assertEquals(
                EventType.FILE_CREATED,
                events.get(0).getEventType()
        );
        assertEquals(
                Severity.LOW,
                events.get(0).getSeverity()
        );
        assertEquals(
                newFile.toAbsolutePath().normalize(),
                events.get(0).getFilePath()
        );
    }

    @Test
    void detectsModifiedFile() throws Exception {
        Path file =
                temporaryDirectory.resolve("important.txt");

        Files.writeString(file, "original");

        List<FileBaseline> baselines =
                baselineService.createFolderBaseline(
                        1,
                        temporaryDirectory
                );

        // These values have the same length.
        // FileGuard must therefore compare their hashes.
        Files.writeString(file, "modified");

        List<SecurityEvent> events =
                monitorService.scanFolder(
                        1,
                        temporaryDirectory,
                        baselines
                );

        assertEquals(1, events.size());
        assertEquals(
                EventType.FILE_MODIFIED,
                events.get(0).getEventType()
        );
        assertEquals(
                Severity.MEDIUM,
                events.get(0).getSeverity()
        );
    }

    @Test
    void detectsDeletedFile() throws Exception {
        Path file =
                temporaryDirectory.resolve("deleted.txt");

        Files.writeString(file, "important data");

        List<FileBaseline> baselines =
                baselineService.createFolderBaseline(
                        1,
                        temporaryDirectory
                );

        Files.delete(file);

        List<SecurityEvent> events =
                monitorService.scanFolder(
                        1,
                        temporaryDirectory,
                        baselines
                );

        assertEquals(1, events.size());
        assertEquals(
                EventType.FILE_DELETED,
                events.get(0).getEventType()
        );
        assertEquals(
                Severity.HIGH,
                events.get(0).getSeverity()
        );
    }

    @Test
    void detectsMultipleTypesOfChanges()
            throws Exception {
        Path safeFile =
                temporaryDirectory.resolve("safe.txt");

        Path modifiedFile =
                temporaryDirectory.resolve("modified.txt");

        Path deletedFile =
                temporaryDirectory.resolve("deleted.txt");

        Files.writeString(safeFile, "safe");
        Files.writeString(modifiedFile, "before");
        Files.writeString(deletedFile, "delete");

        List<FileBaseline> baselines =
                baselineService.createFolderBaseline(
                        1,
                        temporaryDirectory
                );

        Files.writeString(modifiedFile, "after!");
        Files.delete(deletedFile);

        Files.writeString(
                temporaryDirectory.resolve("created.txt"),
                "created"
        );

        List<SecurityEvent> events =
                monitorService.scanFolder(
                        1,
                        temporaryDirectory,
                        baselines
                );

        assertEquals(3, events.size());

        assertTrue(
                containsEventType(
                        events,
                        EventType.FILE_CREATED
                )
        );

        assertTrue(
                containsEventType(
                        events,
                        EventType.FILE_MODIFIED
                )
        );

        assertTrue(
                containsEventType(
                        events,
                        EventType.FILE_DELETED
                )
        );
    }

    @Test
    void rejectsNegativeFolderId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> monitorService.scanFolder(
                        -1,
                        temporaryDirectory,
                        List.of()
                )
        );
    }

    private boolean containsEventType(
            List<SecurityEvent> events,
            EventType eventType
    ) {
        return events.stream()
                .anyMatch(
                        event ->
                                event.getEventType()
                                        == eventType
                );
    }
}