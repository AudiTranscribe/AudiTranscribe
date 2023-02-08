/*
 * QTransformDataObject0x000500.java
 * Description: Data object that stores the Q-Transform data.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package app.auditranscribe.io.audt_file.v0x000500.data_encapsulators;

import app.auditranscribe.generic.tuples.Triple;
import app.auditranscribe.io.ByteConversionHandler;
import app.auditranscribe.io.CompressionHandlers;
import app.auditranscribe.io.audt_file.AUDTFileHelpers;
import app.auditranscribe.io.audt_file.base.data_encapsulators.QTransformDataObject;
import app.auditranscribe.misc.CustomTask;
import app.auditranscribe.utils.TypeConversionUtils;

import java.io.IOException;

/**
 * Data object that stores the Q-Transform data.
 */
public class QTransformDataObject0x000500 extends QTransformDataObject {
    /**
     * Initialization method for the Q-Transform data object.
     *
     * @param qTransformBytes The Q-Transform data as LZ4 compressed bytes.
     * @param maxMagnitude    The maximum magnitude of the Q-Transform data.
     * @param minMagnitude    The minimum magnitude of the Q-Transform data.
     */
    public QTransformDataObject0x000500(byte[] qTransformBytes, double minMagnitude, double maxMagnitude) {
        this.qTransformBytes = qTransformBytes;
        this.minMagnitude = minMagnitude;
        this.maxMagnitude = maxMagnitude;
    }

    // Public methods

    /**
     * Method that converts given Q-Transform magnitude data to byte data.
     *
     * @param magnitudes The Q-Transform magnitude data to convert.
     * @param task       The <code>CustomTask</code>object that is handling the compression of the
     *                   byte data. Pass in <code>null</code> if no such task is being used.
     * @return Triplet of values. First value is the byte data. Second value is the minimum
     * magnitude of the Q-Transform data. Final value is the maximum magnitude of the Q-Transform
     * data.
     * @throws IOException If something went wrong when compressing the bytes.
     */
    public static Triple<Byte[], Double, Double> magnitudesToByteData(
            double[][] magnitudes, CustomTask<?> task
    ) throws IOException {
        // Obtain the byte data
        Triple<Byte[], Double, Double> convertedTuple = magnitudesToUncompressedByteDataHelper(magnitudes);
        byte[] plainBytes = TypeConversionUtils.toByteArray(convertedTuple.value0());
        double min = convertedTuple.value1();
        double max = convertedTuple.value2();

        // Compress the bytes
        byte[] bytes = CompressionHandlers.lz4Compress(plainBytes, task);

        // Return the bytes and the min and max values
        return new Triple<>(TypeConversionUtils.toByteArray(bytes), min, max);
    }

    /**
     * Method that converts given byte data to a Q-Transform magnitude data.
     *
     * @param bytes        The byte data to convert.
     * @param minMagnitude The minimum magnitude of the Q-Transform data.
     * @param maxMagnitude The maximum magnitude of the Q-Transform data.
     * @return The Q-Transform magnitude data.
     * @throws IOException If something went wrong when decompressing the bytes.
     */
    public static double[][] byteDataToMagnitudes(
            byte[] bytes, double minMagnitude, double maxMagnitude
    ) throws IOException {
        // Decompress the bytes
        byte[] plainBytes = CompressionHandlers.lz4Decompress(bytes);

        // Convert bytes to 2D integer array
        int[][] intData = ByteConversionHandler.bytesToTwoDimensionalIntegerArray(plainBytes);

        // Finally convert the integer data to double
        return AUDTFileHelpers.int2DtoDoubles2D(intData, minMagnitude, maxMagnitude);
    }

    @Override
    public int numBytesNeeded() {
        return 4 +  // Section ID
                (4 + qTransformBytes.length) +  // +4 for the length of the Q-Transform data
                8 +  // 8 bytes for the min magnitude
                8 +  // 8 bytes for the max magnitude
                4;   // EOS delimiter
    }
}
