/*
 * KaiserFast.java
 * Description: Kaiser Fast resampling filter.
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

package site.overwrite.auditranscribe.audio.filters;

import site.overwrite.auditranscribe.audio.exceptions.FilterNotFoundException;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.IOException;

/**
 * Kaiser Fast resampling filter.
 *
 * @see <a href="https://resampy.readthedocs.io/en/master/api.html#module-resampy.filters">Kaiser
 * Fast Resampling Filter</a> on Resampy.
 */
public class KaiserFast extends AbstractFilter {
    public KaiserFast() {
        try {
            defineAttributes(IOMethods.joinPaths("filter-data", "kaiser-fast.json"));
        } catch (IOException e) {
            throw new FilterNotFoundException("The Kaiser Fast JSON file could not be located.");
        }
    }
}
