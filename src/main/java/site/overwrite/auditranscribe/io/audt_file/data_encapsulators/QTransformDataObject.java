/*
 * QTransformDataObject.java
 *
 * Created on 2022-05-05
 * Updated on 2022-05-10
 *
 * Description: Data object that stores the Q-Transform data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators;

import java.util.Arrays;

/**
 * Data object that stores the Q-Transform data.
 */
public class QTransformDataObject extends AbstractDataObject {
    // Attributes
    public double[][] qTransformMatrix;

    /**
     * Initialization method for the Q-Transform data object.
     *
     * @param qTransformMatrix   Q-Transform matrix data.
     */
    public QTransformDataObject(double[][] qTransformMatrix) {
        this.qTransformMatrix = qTransformMatrix;
    }

    // Overwritten methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QTransformDataObject that = (QTransformDataObject) o;
        return Arrays.deepEquals(qTransformMatrix, that.qTransformMatrix);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(qTransformMatrix);
    }
}
