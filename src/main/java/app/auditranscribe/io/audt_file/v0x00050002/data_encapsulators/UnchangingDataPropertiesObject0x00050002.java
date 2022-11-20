/*
 * UnchangingDataPropertiesObject0x00050002.java
 * Description: Data object that stores the unchanging data's properties.
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

package app.auditranscribe.io.audt_file.v0x00050002.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.UnchangingDataPropertiesObject;

import java.util.Objects;

/**
 * Data object that stores the unchanging data's properties.
 */
public class UnchangingDataPropertiesObject0x00050002 extends UnchangingDataPropertiesObject {
    /**
     * Initialization method for the unchanging data properties object.
     *
     * @param numSkippableBytes The number of skippable bytes.
     */
    public UnchangingDataPropertiesObject0x00050002(int numSkippableBytes) {
        this.numSkippableBytes = numSkippableBytes;
    }

    // Overridden methods
    @Override
    public int numBytesNeeded() {
        return NUM_BYTES_NEEDED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnchangingDataPropertiesObject0x00050002 that = (UnchangingDataPropertiesObject0x00050002) o;
        return numSkippableBytes == that.numSkippableBytes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numSkippableBytes);
    }
}
