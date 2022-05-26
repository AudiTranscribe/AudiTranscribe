/*
 * KaiserFast.java
 *
 * Created on 2022-03-07
 * Updated on 2022-05-25
 *
 * Description: Kaiser Fast resampling filter.
 */

package site.overwrite.auditranscribe.audio.filters;

/**
 * Kaiser Fast resampling filter.
 *
 * @see <a href="https://resampy.readthedocs.io/en/master/api.html#module-resampy.filters">Kaiser
 * Fast Resampling Filter</a> on Resampy.
 */
public class KaiserFast extends AbstractFilter {
    public KaiserFast() {
        defineAttributes("filter-data/kaiser-fast.json");
    }
}
