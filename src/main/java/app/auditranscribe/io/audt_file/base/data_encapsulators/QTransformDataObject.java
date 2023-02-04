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

import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

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
}
