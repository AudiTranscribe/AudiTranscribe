/*
 * QTransformDataObject.java
 *
 * Created on 2022-05-05
 * Updated on 2022-06-21
 *
 * Description: Data object that stores the Q-Transform data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators;

import org.javatuples.Triplet;
import site.overwrite.auditranscribe.io.IOConverters;
import site.overwrite.auditranscribe.io.LZ4;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileHelpers;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.utils.TypeConversionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Data object that stores the Q-Transform data.
 */
public class QTransformDataObject extends AbstractAUDTDataObject {
    // Constants
    public static final int SECTION_ID = 2;

    // Attributes
    public byte[] qTransformBytes;

    public double minMagnitude;
    public double maxMagnitude;

    /**
     * Initialization method for the Q-Transform data object.
     *
     * @param qTransformBytes The Q-Transform data as LZ4 compressed bytes.
     * @param maxMagnitude    The maximum magnitude of the Q-Transform data.
     * @param minMagnitude    The minimum magnitude of the Q-Transform data.
     */
    public QTransformDataObject(byte[] qTransformBytes, double minMagnitude, double maxMagnitude) {
        // Update attributes
        this.qTransformBytes = qTransformBytes;
        this.minMagnitude = minMagnitude;
        this.maxMagnitude = maxMagnitude;
    }

    // Overwritten methods
    @Override
    public int numBytesNeeded() {
        return 4 +  // Section ID
                (4 + qTransformBytes.length) +  // +4 for the length of the Q-Transform data
                8 +  // 8 bytes for the min magnitude
                8 +  // 8 bytes for the max magnitude
                4;   // EOS delimiter
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QTransformDataObject that = (QTransformDataObject) o;
        return (
                Double.compare(that.minMagnitude, minMagnitude) == 0 &&
                        Double.compare(that.maxMagnitude, maxMagnitude) == 0 &&
                        Arrays.equals(qTransformBytes, that.qTransformBytes)
        );
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(minMagnitude, maxMagnitude);
        result = 31 * result + Arrays.hashCode(qTransformBytes);
        return result;
    }


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
    public static Triplet<Byte[], Double, Double> qTransformMagnitudesToByteData(
            double[][] qTransformMagnitudes, CustomTask<?> task
    ) throws IOException {
        // Convert the double data to integer data
        Triplet<Integer[][], Double, Double> convertedTuple = AUDTFileHelpers.doubles2DtoInt2D(qTransformMagnitudes);
        Integer[][] intData = convertedTuple.getValue0();
        double min = convertedTuple.getValue1();
        double max = convertedTuple.getValue2();

        // Convert non-primitive integers to primitive integers
        int[][] intDataPrimitive = new int[intData.length][intData[0].length];
        for (int i = 0; i < intData.length; i++) {
            intDataPrimitive[i] = TypeConversionUtils.toIntegerArray(intData[i]);
        }

        // Convert the integer data to bytes
        byte[] plainBytes = IOConverters.twoDimensionalIntegerArrayToBytes(intDataPrimitive);

        // Compress the bytes
        byte[] bytes = LZ4.lz4Compress(plainBytes, task);

        // Return the bytes and the min and max values
        return new Triplet<>(TypeConversionUtils.toByteArray(bytes), min, max);
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
        byte[] plainBytes = LZ4.lz4Decompress(bytes);

        // Convert bytes to 2D integer array
        int[][] intData = IOConverters.bytesToTwoDimensionalIntegerArray(plainBytes);

        // Finally convert the integer data to double
        return AUDTFileHelpers.int2DtoDoubles2D(intData, minMagnitude, maxMagnitude);
    }
}
