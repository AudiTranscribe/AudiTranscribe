/*
 * KaiserBest.java
 *
 * Created on 2022-03-07
 * Updated on 2022-07-01
 *
 * Description: Kaiser Best resampling filter.
 */

package site.overwrite.auditranscribe.audio.filters;

import site.overwrite.auditranscribe.exceptions.audio.FilterNotFoundException;
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
