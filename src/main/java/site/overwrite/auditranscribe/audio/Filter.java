/*
 * Filter.java
 * Description: Enum that contains resampling filters to be used during signal processing.
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

package site.overwrite.auditranscribe.audio;

import site.overwrite.auditranscribe.audio.filters.AbstractFilter;
import site.overwrite.auditranscribe.audio.filters.KaiserBest;
import site.overwrite.auditranscribe.audio.filters.KaiserFast;

/**
 * Enum that contains resampling filters to be used during signal processing.
 */
public enum Filter {
    // Enum values
    KAISER_BEST(new KaiserBest()),
    KAISER_FAST(new KaiserFast());

    // Attributes
    public final AbstractFilter filter;

    // Enum constructor
    Filter(AbstractFilter filter) {
        this.filter = filter;
    }
}
