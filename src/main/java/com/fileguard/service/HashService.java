package com.fileguard.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashService {

    /**
     * Calculates the SHA-256 hash of a file.
     *
     * @param file path of the file to hash
     * @return SHA-256 hash as a hexadecimal string
     * @throws IOException if the file cannot be read
     */
    public String calculateSHA256(Path file) throws IOException {
        validateFile(file);

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            try (InputStream inputStream =
                         Files.newInputStream(file)) {

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] hashBytes = digest.digest();

            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable.",
                    exception
            );
        }
    }

    /**
     * Checks whether the selected path is a readable file.
     *
     * @param file path to validate
     */
    private void validateFile(Path file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "The file path cannot be null."
            );
        }

        if (!Files.exists(file)) {
            throw new IllegalArgumentException(
                    "The selected file does not exist."
            );
        }

        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException(
                    "The selected path is not a regular file."
            );
        }

        if (!Files.isReadable(file)) {
            throw new IllegalArgumentException(
                    "The selected file cannot be read."
            );
        }
    }
}