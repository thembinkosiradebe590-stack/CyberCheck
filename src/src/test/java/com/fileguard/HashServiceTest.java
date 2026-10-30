package com.fileguard;

import com.fileguard.service.HashService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashServiceTest {

    private final HashService hashService =
            new HashService();

    @TempDir
    Path temporaryDirectory;

    @Test
    void calculatesKnownSHA256Hash() throws Exception {
        Path testFile =
                temporaryDirectory.resolve("hello.txt");

        Files.writeString(testFile, "hello");

        String actualHash =
                hashService.calculateSHA256(testFile);

        String expectedHash =
                "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";

        assertEquals(expectedHash, actualHash);
    }

    @Test
    void differentContentProducesDifferentHashes()
            throws Exception {

        Path firstFile =
                temporaryDirectory.resolve("first.txt");

        Path secondFile =
                temporaryDirectory.resolve("second.txt");

        Files.writeString(firstFile, "original");
        Files.writeString(secondFile, "modified");

        String firstHash =
                hashService.calculateSHA256(firstFile);

        String secondHash =
                hashService.calculateSHA256(secondFile);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void rejectsMissingFiles() {
        Path missingFile =
                temporaryDirectory.resolve("missing.txt");

        assertThrows(
                IllegalArgumentException.class,
                () -> hashService.calculateSHA256(missingFile)
        );
    }

    @Test
    void rejectsNullPaths() {
        assertThrows(
                IllegalArgumentException.class,
                () -> hashService.calculateSHA256(null)
        );
    }
}