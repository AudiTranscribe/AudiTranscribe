/*
 * KaiserBest.java
 * Description: Kaiser Best resampling filter.
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
 * Kaiser Best resampling filter.
 *
 * @see <a href="https://resampy.readthedocs.io/en/master/api.html#module-resampy.filters">Kaiser
 * Best Resampling Filter</a> on Resampy.
 */
public class KaiserBest extends AbstractFilter {
    public KaiserBest() {
        try {
            defineAttributes(IOMethods.joinPaths("filter-data", "kaiser-best.json"));
        } catch (IOException e) {
            throw new FilterNotFoundException("The Kaiser Best JSON file could not be located.");
        }
    }
}
