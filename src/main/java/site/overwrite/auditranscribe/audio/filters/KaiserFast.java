/*
 * KaiserFast.java
 *
 * Created on 2022-03-07
 * Updated on 2022-07-01
 *
 * Description: Kaiser Fast resampling filter.
 */

package site.overwrite.auditranscribe.audio.filters;

import site.overwrite.auditranscribe.exceptions.audio.FilterNotFoundException;
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
