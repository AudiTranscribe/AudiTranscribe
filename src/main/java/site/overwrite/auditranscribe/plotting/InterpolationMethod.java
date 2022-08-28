/*
 * InterpolationMethod.java
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

package site.overwrite.auditranscribe.plotting;

import site.overwrite.auditranscribe.plotting.interpolation_methods.Bilinear;
import site.overwrite.auditranscribe.plotting.interpolation_methods.AbstractInterpolation;
import site.overwrite.auditranscribe.plotting.interpolation_methods.NearestNeighbour;

/**
 * Enum that contains the different interpolation methods that can be used on a 2D array.
 */
public enum InterpolationMethod {
    // Enum values
    NEAREST_NEIGHBOUR(new NearestNeighbour()),
    BILINEAR(new Bilinear());

    // Attributes
    final AbstractInterpolation interpolation;

    // Enum constructor
    InterpolationMethod(AbstractInterpolation interpolation) {
        this.interpolation = interpolation;
    }
}
