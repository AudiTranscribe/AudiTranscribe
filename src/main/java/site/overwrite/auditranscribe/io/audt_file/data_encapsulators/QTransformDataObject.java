/*
 * QTransformDataObject.java
 *
 * Created on 2022-05-05
 * Updated on 2022-06-05
 *
 * Description: Data object that stores the Q-Transform data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators;

import site.overwrite.auditranscribe.io.audt_file.AUDTFileHelpers;

import java.util.Arrays;

/**
 * Data object that stores the Q-Transform data.
 */
public class QTransformDataObject extends AbstractDataObject {
    // Attributes
    public double[][] qTransformMagnitudes;

    public double minMagnitude = Double.MAX_VALUE;
    public double maxMagnitude = -Double.MAX_VALUE;

    /**
     * Initialization method for the Q-Transform data object.
     *
     * @param qTransformMagnitudes Q-Transform magnitude data.
     */
    public QTransformDataObject(double[][] qTransformMagnitudes) {
        // Update attributes
        this.qTransformMagnitudes = qTransformMagnitudes;

        // Get the minimum and maximum magnitudes
        for (double[] row : qTransformMagnitudes) {
            for (double magnitude : row) {
                if (magnitude < minMagnitude) minMagnitude = magnitude;
                if (magnitude > maxMagnitude) maxMagnitude = magnitude;
            }
        }
    }

    /**
     * Initialization method for the Q-Transform data object.
     *
     * @param qTransformMagnitudes Q-Transform magnitude data. The integers received in this 2D
     *                             array are the normalized values of the magnitudes, in the
     *                             interval [-2147483648, 2147483647].
     */
    public QTransformDataObject(int[][] qTransformMagnitudes, double minMagnitude, double maxMagnitude) {
        // Update attributes
        this.minMagnitude = minMagnitude;
        this.maxMagnitude = maxMagnitude;

        // Convert the integers back to double and update the Q-Transform data
        this.qTransformMagnitudes = AUDTFileHelpers.int2DtoDoubles2D(qTransformMagnitudes, minMagnitude, maxMagnitude);
    }

    // Public methods

    /**
     * Method that returns the integer representation of the double magnitudes.
     */
    public int[][] getIntegerMagnitudeValues() {
        return AUDTFileHelpers.doubles2DtoInt2D(qTransformMagnitudes);
    }

    // Overwritten methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QTransformDataObject that = (QTransformDataObject) o;
        return Arrays.deepEquals(qTransformMagnitudes, that.qTransformMagnitudes);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(qTransformMagnitudes);
    }
}
