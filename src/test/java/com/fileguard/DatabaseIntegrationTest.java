
package com.fileguard;

import com.fileguard.database.DatabaseInitializer;
import com.fileguard.database.DatabaseManager;
import com.fileguard.model.EventStatus;
import com.fileguard.model.EventType;
import com.fileguard.model.FileBaseline;
import com.fileguard.model.MonitoredFolder;
import com.fileguard.model.SecurityEvent;
import com.fileguard.model.Severity;
import com.fileguard.repository.BaselineRepository;
import com.fileguard.repository.EventRepository;
import com.fileguard.repository.FolderRepository;
import com.fileguard.service.BaselineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseIntegrationTest {

    @TempDir
    Path temporaryDirectory;

    private FolderRepository folderRepository;
    private BaselineRepository baselineRepository;
    private EventRepository eventRepository;

    @BeforeEach
    void initializeDatabase() throws Exception {
        Path databaseFile =
                temporaryDirectory.resolve(
                        "fileguard-test.db"
                );

        DatabaseManager databaseManager =
                new DatabaseManager(databaseFile);

        DatabaseInitializer initializer =
                new DatabaseInitializer(databaseManager);

        initializer.initialize();

        folderRepository =
                new FolderRepository(databaseManager);

        baselineRepository =
                new BaselineRepository(databaseManager);

        eventRepository =
                new EventRepository(databaseManager);
    }

    @Test
    void savesAndFindsMonitoredFolder()
            throws Exception {
        Path watchedFolder =
                temporaryDirectory.resolve("watched");

        Files.createDirectories(watchedFolder);

        MonitoredFolder folder =
                new MonitoredFolder(
                        1,
                        watchedFolder
                );

        MonitoredFolder savedFolder =
                folderRepository.save(folder);

        assertTrue(savedFolder.getId() > 0);

        MonitoredFolder foundFolder =
                folderRepository
                        .findById(savedFolder.getId())
                        .orElseThrow();

        assertEquals(
                savedFolder.getId(),
                foundFolder.getId()
        );

        assertEquals(
                watchedFolder
                        .toAbsolutePath()
                        .normalize(),
                foundFolder.getFolderPath()
        );
    }

    @Test
    void savesAndLoadsFileBaseline()
            throws Exception {
        Path watchedFolder =
                temporaryDirectory.resolve("watched");

        Files.createDirectories(watchedFolder);

        MonitoredFolder savedFolder =
                folderRepository.save(
                        new MonitoredFolder(
                                1,
                                watchedFolder
                        )
                );

        Path file =
                watchedFolder.resolve("important.txt");

        Files.writeString(file, "important data");

        BaselineService baselineService =
                new BaselineService();

        FileBaseline baseline =
                baselineService.createBaseline(
                        savedFolder.getId(),
                        file
                );

        FileBaseline savedBaseline =
                baselineRepository.save(baseline);

        List<FileBaseline> loadedBaselines =
                baselineRepository.findByFolderId(
                        savedFolder.getId()
                );

        assertTrue(savedBaseline.getId() > 0);
        assertEquals(1, loadedBaselines.size());

        assertEquals(
                savedBaseline.getSha256Hash(),
                loadedBaselines.get(0).getSha256Hash()
        );

        assertEquals(
                savedBaseline.getFilePath(),
                loadedBaselines.get(0).getFilePath()
        );
    }

    @Test
    void savesAndUpdatesSecurityEvent()
            throws Exception {
        Path watchedFolder =
                temporaryDirectory.resolve("watched");

        Files.createDirectories(watchedFolder);

        MonitoredFolder savedFolder =
                folderRepository.save(
                        new MonitoredFolder(
                                1,
                                watchedFolder
                        )
                );

        SecurityEvent event =
                new SecurityEvent(
                        savedFolder.getId(),
                        watchedFolder.resolve(
                                "changed.txt"
                        ),
                        EventType.FILE_MODIFIED,
                        Severity.MEDIUM,
                        "The monitored file was modified."
                );

        SecurityEvent savedEvent =
                eventRepository.save(event);

        assertTrue(savedEvent.getId() > 0);

        boolean updated =
                eventRepository.updateStatus(
                        savedEvent.getId(),
                        EventStatus.RESOLVED
                );

        assertTrue(updated);

        List<SecurityEvent> loadedEvents =
                eventRepository.findByFolderId(
                        savedFolder.getId()
                );

        assertEquals(1, loadedEvents.size());

        assertEquals(
                EventStatus.RESOLVED,
                loadedEvents.get(0).getStatus()
        );

        assertEquals(
                EventType.FILE_MODIFIED,
                loadedEvents.get(0).getEventType()
        );
    }
}