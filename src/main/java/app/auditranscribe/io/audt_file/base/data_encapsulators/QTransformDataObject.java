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

package app.auditranscribe.io.audt_file.base.data_encapsulators;

import app.auditranscribe.generic.tuples.Triple;
import app.auditranscribe.io.ByteConversionHandler;
import app.auditranscribe.io.audt_file.AUDTFileHelpers;
import app.auditranscribe.misc.CustomTask;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.utils.TypeConversionUtils;

import java.util.Arrays;
import java.util.Objects;

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
     * Converts the provided magnitudes into attributes' values.
     *
     * @param magnitudes Magnitudes of the spectrogram.
     * @param task       A <code>CustomTask</code> instance used to track the compression
     *                   progress.<br>
     *                   Pass in <code>null</code> if not using a task.
     */
    public abstract void magnitudesToSaveData(double[][] magnitudes, CustomTask<?> task);

    /**
     * Converts the attributes' values to magnitude data.
     *
     * @return Magnitude data.
     */
    public abstract double[][] saveDataToMagnitudes();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QTransformDataObject that = (QTransformDataObject) o;
        return Arrays.equals(qTransformBytes, that.qTransformBytes) &&
                Double.compare(that.minMagnitude, minMagnitude) == 0 &&
                Double.compare(that.maxMagnitude, maxMagnitude) == 0;
    }

    @Override
    @ExcludeFromGeneratedCoverageReport
    public int hashCode() {
        int result = Objects.hash(minMagnitude, maxMagnitude);
        result = 31 * result + Arrays.hashCode(qTransformBytes);
        return result;
    }

    // Protected methods

    /**
     * Helper method that converts the provided magnitudes into byte data.
     *
     * @param magnitudes The Q-Transform magnitude data to convert.
     * @return Triplet of values. First value is the byte data. Second value is the minimum
     * magnitude of the Q-Transform data. Final value is the maximum magnitude of the Q-Transform
     * data.
     */
    protected static Triple<Byte[], Double, Double> magnitudesToUncompressedByteDataHelper(double[][] magnitudes) {
        // Convert the double data to integer data
        Triple<Integer[][], Double, Double> convertedTuple = AUDTFileHelpers.doubles2DtoInt2D(magnitudes);
        Integer[][] intData = convertedTuple.value0();
        double min = convertedTuple.value1();
        double max = convertedTuple.value2();

        // Convert non-primitive integers to primitive integers
        int[][] intDataPrimitive = new int[intData.length][intData[0].length];
        for (int i = 0; i < intData.length; i++) {
            intDataPrimitive[i] = TypeConversionUtils.toIntegerArray(intData[i]);
        }

        // Convert the integer data to bytes
        byte[] plainBytes = ByteConversionHandler.twoDimensionalIntegerArrayToBytes(intDataPrimitive);

        return new Triple<>(TypeConversionUtils.toByteArray(plainBytes), min, max);
    }
}
