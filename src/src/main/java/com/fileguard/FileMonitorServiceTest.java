package com.fileguard;

import com.fileguard.database.DatabaseInitializer;
import com.fileguard.model.EventType;
import com.fileguard.model.MonitoredFolder;
import com.fileguard.repository.BaselineRepository;
import com.fileguard.repository.EventRepository;
import com.fileguard.repository.FolderRepository;
import com.fileguard.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileMonitorServiceTest {
    @TempDir Path tempDirectory;

    @Test
    void detectsModificationOfBaselinedFile() throws Exception {
        System.setProperty("fileguard.db.path", tempDirectory.resolve("monitor.db").toString());
        DatabaseInitializer.initialize();
        FolderRepository folders = new FolderRepository();
        BaselineRepository baselines = new BaselineRepository();
        EventRepository events = new EventRepository();
        HashService hashes = new HashService();
        Path protectedFolder = Files.createDirectory(tempDirectory.resolve("protected"));
        Path file = protectedFolder.resolve("sample.txt");
        Files.writeString(file, "before");
        MonitoredFolder folder = folders.save(new MonitoredFolder(protectedFolder));
        new BaselineService(hashes, baselines, folders).createBaseline(folder);
        CountDownLatch detected = new CountDownLatch(1);

        try (FileMonitorService monitor = new FileMonitorService(hashes, baselines, events, folders, new AlertService())) {
            monitor.startMonitoring(folder, event -> {
                if (event.getEventType() == EventType.MODIFIED) detected.countDown();
            });
            Files.writeString(file, "after");
            assertTrue(detected.await(5, TimeUnit.SECONDS), "Expected a modification event");
        }
    }
}