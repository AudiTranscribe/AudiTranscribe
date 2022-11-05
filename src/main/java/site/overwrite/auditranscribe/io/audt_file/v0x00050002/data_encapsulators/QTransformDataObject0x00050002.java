/*
 * QTransformDataObject0x00050002.java
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

package site.overwrite.auditranscribe.io.audt_file.v0x00050002.data_encapsulators;

import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.QTransformDataObject;

import java.util.Arrays;
import java.util.Objects;

/**
 * Data object that stores the Q-Transform data.
 */
public class QTransformDataObject0x00050002 extends QTransformDataObject {
    /**
     * Initialization method for the Q-Transform data object.
     *
     * @param qTransformBytes The Q-Transform data as LZ4 compressed bytes.
     * @param maxMagnitude    The maximum magnitude of the Q-Transform data.
     * @param minMagnitude    The minimum magnitude of the Q-Transform data.
     */
    public QTransformDataObject0x00050002(byte[] qTransformBytes, double minMagnitude, double maxMagnitude) {
        this.qTransformBytes = qTransformBytes;
        this.minMagnitude = minMagnitude;
        this.maxMagnitude = maxMagnitude;
    }

    // Overridden methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QTransformDataObject0x00050002 that = (QTransformDataObject0x00050002) o;
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

    @Override
    public int numBytesNeeded() {
        return 4 +  // Section ID
                (4 + qTransformBytes.length) +  // +4 for the length of the Q-Transform data
                8 +  // 8 bytes for the min magnitude
                8 +  // 8 bytes for the max magnitude
                4;   // EOS delimiter
    }
}
