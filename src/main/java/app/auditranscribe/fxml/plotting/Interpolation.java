/*
 * Interpolation.java
 * Description: Enum that contains the different interpolation methods that can be used on a 2D
 *              array.
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

package app.auditranscribe.fxml.plotting;

import app.auditranscribe.fxml.plotting.interpolation.AbstractInterpolation;
import app.auditranscribe.fxml.plotting.interpolation.BilinearInterpolation;
import app.auditranscribe.fxml.plotting.interpolation.NearestNeighbourInterpolation;

/**
 * Enum that contains the different interpolation methods that can be used on a 2D array.
 */
public enum Interpolation {
    // Enum values
    NEAREST_NEIGHBOUR(new NearestNeighbourInterpolation()),
    BILINEAR(new BilinearInterpolation());

    // Attributes
    final AbstractInterpolation interpolation;

    // Enum constructor
    Interpolation(AbstractInterpolation interpolation) {
        this.interpolation = interpolation;
    }
}
