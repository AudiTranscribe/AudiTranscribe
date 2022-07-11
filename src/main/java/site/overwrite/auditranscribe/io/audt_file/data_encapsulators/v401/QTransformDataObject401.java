/*
 * QTransformDataObject401.java
 *
 * Created on 2022-07-11
 * Updated on 2022-07-11
 *
 * Description: Data object that stores the Q-Transform data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators.v401;

import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.QTransformDataObject;

/**
 * Data object that stores the Q-Transform data.
 */
public class QTransformDataObject401 extends QTransformDataObject {
    /**
     * Initialization method for the Q-Transform data object.
     *
     * @param qTransformBytes The Q-Transform data as LZ4 compressed bytes.
     * @param maxMagnitude    The maximum magnitude of the Q-Transform data.
     * @param minMagnitude    The minimum magnitude of the Q-Transform data.
     */
    public QTransformDataObject401(byte[] qTransformBytes, double minMagnitude, double maxMagnitude) {
        // Update attributes
        this.qTransformBytes = qTransformBytes;
        this.minMagnitude = minMagnitude;
        this.maxMagnitude = maxMagnitude;
    }
}
