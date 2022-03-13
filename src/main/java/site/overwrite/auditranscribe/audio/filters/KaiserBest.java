/*
 * KaiserFast.java
 *
 * Created on 2022-03-07
 * Updated on 2022-03-07
 *
 * Description: Kaiser Best filter.
 *
 * See: https://resampy.readthedocs.io/en/master/api.html#module-resampy.filters
 */

package site.overwrite.auditranscribe.audio.filters;

public class KaiserBest extends AbstractFilter {
    public KaiserBest() {
        defineAttributes("filter_data_json/kaiser_best.json");
    }
}
