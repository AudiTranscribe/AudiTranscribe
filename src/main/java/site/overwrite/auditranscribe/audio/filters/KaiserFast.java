/*
 * KaiserFast.java
 *
 * Created on 2022-03-07
 * Updated on 2022-03-15
 *
 * Description: Kaiser Fast resampling filter.
 */

package site.overwrite.auditranscribe.audio.filters;

/**
 * Kaiser fast resampling filter.
 *
 * @see <a href="https://resampy.readthedocs.io/en/master/api.html#module-resampy.filters">Kaiser
 * Fast Resampling Filter</a> on Resampy.
 */
public class KaiserFast extends AbstractFilter {
    public KaiserFast() {
        defineAttributes("filter_data_json/kaiser_fast.json");
    }
}
