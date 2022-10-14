/*
 * QTransformDataObject.java
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

package site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators;

import site.overwrite.auditranscribe.generic.tuples.Triple;
import site.overwrite.auditranscribe.io.CompressionHandlers;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileHelpers;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.utils.ByteConversionUtils;
import site.overwrite.auditranscribe.utils.TypeConversionUtils;

import java.io.IOException;

/**
 * Data object that stores the Q-Transform data.
 */
public abstract class QTransformDataObject extends AbstractAUDTDataObject {
    // Constants
    public static final int SECTION_ID = 2;

    // Attributes
    public byte[] qTransformBytes;

    public double minMagnitude;
    public double maxMagnitude;

    // Public methods

    /**
     * Method that converts given Q-Transform magnitude data to byte data.
     *
     * @param qTransformMagnitudes The Q-Transform magnitude data to convert.
     * @param task                 The <code>CustomTask</code>object that is handling the
     *                             generation. Pass in <code>null</code> if no such task is being
     *                             used.
     * @return Triplet of values. First value is the byte data. Second value is the minimum
     * magnitude of the Q-Transform data. Final value is the maximum magnitude of the Q-Transform
     * data.
     * @throws IOException If something went wrong when compressing the bytes.
     */
    public static Triple<Byte[], Double, Double> qTransformMagnitudesToByteData(
            double[][] qTransformMagnitudes, CustomTask<?> task
    ) throws IOException {
        // Convert the double data to integer data
        Triple<Integer[][], Double, Double> convertedTuple = AUDTFileHelpers.doubles2DtoInt2D(qTransformMagnitudes);
        Integer[][] intData = convertedTuple.value0();
        double min = convertedTuple.value1();
        double max = convertedTuple.value2();

        // Convert non-primitive integers to primitive integers
        int[][] intDataPrimitive = new int[intData.length][intData[0].length];
        for (int i = 0; i < intData.length; i++) {
            intDataPrimitive[i] = TypeConversionUtils.toIntegerArray(intData[i]);
        }

        // Convert the integer data to bytes
        byte[] plainBytes = ByteConversionUtils.twoDimensionalIntegerArrayToBytes(intDataPrimitive);

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
    public static double[][] byteDataToQTransformMagnitudes(
            byte[] bytes, double minMagnitude, double maxMagnitude
    ) throws IOException {
        // Decompress the bytes
        byte[] plainBytes = CompressionHandlers.lz4Decompress(bytes);

        // Convert bytes to 2D integer array
        int[][] intData = ByteConversionUtils.bytesToTwoDimensionalIntegerArray(plainBytes);

        // Finally convert the integer data to double
        return AUDTFileHelpers.int2DtoDoubles2D(intData, minMagnitude, maxMagnitude);
    }
}
