/*
 * Theme.java
 * Description: Enum that contains the different possible themes that AudiTranscribe supports.
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

package app.auditranscribe.fxml;

import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

/**
 * Enum that contains the different possible themes that AudiTranscribe supports.
 */
@ExcludeFromGeneratedCoverageReport
public enum Theme {
    // Enum values
    LIGHT("Light", "light"),
    DARK("Dark", "dark"),
    HIGH_CONTRAST("High Contrast", "high-contrast");

    // Enum attributes
    private final String name;
    public final String shortName;
    public final String cssFile;

    // Enum constructor
    Theme(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
        this.cssFile = shortName + ".css";
    }

    // Overridden methods
    @Override
    public String toString() {
        return name;
    }
}
