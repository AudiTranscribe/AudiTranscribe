/*
 * QTransformDataObject0x00090002.java
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

package app.auditranscribe.io.audt_file.v0x00090002.data_encapsulators;

import app.auditranscribe.io.audt_file.v0x00080001.data_encapsulators.QTransformDataObject0x00080001;

/**
 * Data object that stores the Q-Transform data.
 */
public class QTransformDataObject0x00090002 extends QTransformDataObject0x00080001 {
    /**
     * Initialization method for the Q-Transform data object.
     *
     * @param qTransformBytes The Q-Transform data as LZ4 compressed bytes.
     * @param minMagnitude    The minimum magnitude of the Q-Transform data.
     * @param maxMagnitude    The maximum magnitude of the Q-Transform data.
     */
    public QTransformDataObject0x00090002(byte[] qTransformBytes, double minMagnitude, double maxMagnitude) {
        super(qTransformBytes, minMagnitude, maxMagnitude);
    }
}
