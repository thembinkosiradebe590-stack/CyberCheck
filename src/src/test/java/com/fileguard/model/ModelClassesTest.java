package com.fileguard.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelClassesTest {

    private static final String EXAMPLE_HASH =
            "2cf24dba5fb0a30e26e83b2ac5b9e29"
                    + "e1b161e5c1fa7425e73043362938b9824";

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsUserWithSafeStoredValues() {
        User user = new User(
                " student ",
                "$2a$10$exampleHash"
        );

        assertEquals("student", user.getUsername());
        assertEquals(
                "$2a$10$exampleHash",
                user.getPasswordHash()
        );
        assertEquals(0, user.getId());
    }

    @Test
    void rejectsBlankUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        "  ",
                        "$2a$10$exampleHash"
                )
        );
    }

    @Test
    void activatesAndDeactivatesFolderMonitoring() {
        MonitoredFolder folder = new MonitoredFolder(
                1,
                temporaryDirectory
        );

        assertTrue(folder.isActive());

        folder.deactivate();
        assertFalse(folder.isActive());

        folder.activate();
        assertTrue(folder.isActive());
    }

    @Test
    void baselineRecognizesMatchingHash() {
        FileBaseline baseline = new FileBaseline(
                1,
                temporaryDirectory.resolve("hello.txt"),
                EXAMPLE_HASH,
                5,
                Instant.now()
        );

        assertTrue(baseline.matchesHash(EXAMPLE_HASH));
        assertFalse(
                baseline.matchesHash("0".repeat(64))
        );
    }

    @Test
    void rejectsInvalidBaselineHash() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileBaseline(
                        1,
                        temporaryDirectory.resolve("hello.txt"),
                        "not-a-sha-256-hash",
                        5,
                        Instant.now()
                )
        );
    }

    @Test
    void movesSecurityEventThroughReviewStates() {
        SecurityEvent event = new SecurityEvent(
                1,
                temporaryDirectory.resolve("important.txt"),
                EventType.FILE_MODIFIED,
                Severity.HIGH,
                "The file hash changed."
        );

        assertEquals(
                EventStatus.OPEN,
                event.getStatus()
        );

        event.acknowledge();

        assertEquals(
                EventStatus.ACKNOWLEDGED,
                event.getStatus()
        );

        event.resolve();

        assertEquals(
                EventStatus.RESOLVED,
                event.getStatus()
        );
    }

    @Test
    void preventsAcknowledgingResolvedEvent() {
        SecurityEvent event = new SecurityEvent(
                1,
                temporaryDirectory.resolve("important.txt"),
                EventType.FILE_DELETED,
                Severity.HIGH,
                "The monitored file was deleted."
        );

        event.resolve();

        assertThrows(
                IllegalStateException.class,
                event::acknowledge
        );
    }
}