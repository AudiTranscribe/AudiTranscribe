/*
 * KaiserBest.java
 *
 * Created on 2022-03-07
 * Updated on 2022-03-15
 *
 * Description: Kaiser Best resampling filter.
 */

package site.overwrite.auditranscribe.audio.filters;

/**
 * Kaiser best resampling filter.
 *
 * @see <a href="https://resampy.readthedocs.io/en/master/api.html#module-resampy.filters">Kaiser
 * Best Resampling Filter</a> on Resampy.
 */
public class KaiserBest extends AbstractFilter {
    public KaiserBest() {
        defineAttributes("filter_data_json/kaiser_best.json");
    }
}
