/*
 * KaiserFast.java
 *
 * Created on 2022-03-07
 * Updated on 2022-06-24
 *
 * Description: Kaiser Fast resampling filter.
 */

package site.overwrite.auditranscribe.audio.filters;

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
            defineAttributes("filter-data/kaiser-fast.json");
        } catch (IOException ignored) {
        }
    }
}
