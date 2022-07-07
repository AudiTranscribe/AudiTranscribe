/*
 * HashingUtils.java
 *
 * Created on 2022-07-07
 * Updated on 2022-07-07
 *
 * Description: Hashing utility methods.
 */

package site.overwrite.auditranscribe.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hashing utility methods.
 */
public final class HashingUtils {
    private HashingUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Gets the hash value of a byte array.
     *
     * @param bytes     Bytes to hash.
     * @param algorithm Algorithm to use to hash the byte array.
     * @return The hash of the byte array.
     * @throws NoSuchAlgorithmException If the specified algorithm could not be found.
     */
    public static String getHash(byte[] bytes, String algorithm) throws NoSuchAlgorithmException {
        // Get the algorithm to generate the hash
        MessageDigest md = MessageDigest.getInstance(algorithm);

        // Update digest with the bytes to hash
        md.update(bytes);

        // Complete the hash computation by digesting
        byte[] digest = md.digest();

        // Convert bytes to hex and return
        return HexFormat.of().formatHex(digest);
    }

    /**
     * Gets the hash value of a string.
     *
     * @param valueToHash String to hash.
     * @param algorithm   Algorithm to use to hash the string.
     * @return The hash of the string.
     * @throws NoSuchAlgorithmException If the specified algorithm could not be found.
     */
    public static String getHash(String valueToHash, String algorithm) throws NoSuchAlgorithmException {
        return getHash(valueToHash.getBytes(), algorithm);
    }

    /**
     * Gets the hash value of a file.
     *
     * @param file      File to compute the hash of.
     * @param algorithm Algorithm to use to hash the file.
     * @return The hash of the file.
     * @throws NoSuchAlgorithmException If the specified algorithm could not be found.
     */
    public static String getHash(File file, String algorithm) throws NoSuchAlgorithmException, IOException {
        return getHash(Files.readAllBytes(Paths.get(file.getAbsolutePath())), algorithm);
    }
}
