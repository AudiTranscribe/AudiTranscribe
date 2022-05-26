/*
 * KaiserBest.java
 *
 * Created on 2022-03-07
 * Updated on 2022-05-25
 *
 * Description: Kaiser Best resampling filter.
 */

package site.overwrite.auditranscribe.audio.filters;

/**
 * Kaiser Best resampling filter.
 *
 * @see <a href="https://resampy.readthedocs.io/en/master/api.html#module-resampy.filters">Kaiser
 * Best Resampling Filter</a> on Resampy.
 */
public class KaiserBest extends AbstractFilter {
    public KaiserBest() {
        defineAttributes("filter-data/kaiser-best.json");
    }
}
