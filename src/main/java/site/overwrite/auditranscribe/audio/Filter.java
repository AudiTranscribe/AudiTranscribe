/*
 * Filter.java
 *
 * Created on 2022-03-10
 * Updated on 2022-03-15
 *
 * Description: Enum that contains resampling filters.
 */

package site.overwrite.auditranscribe.audio;

import site.overwrite.auditranscribe.audio.filters.AbstractFilter;
import site.overwrite.auditranscribe.audio.filters.KaiserBest;
import site.overwrite.auditranscribe.audio.filters.KaiserFast;

/**
 * Filter enum.<br>
 * Contains resampling filters to be used during signal processing.
 */
public enum Filter {
    // Enum values
    KAISER_BEST(new KaiserBest()),
    KAISER_FAST(new KaiserFast());

    // Attributes
    public final AbstractFilter filter;

    // Enum constructor
    Filter(AbstractFilter filter) {
        this.filter = filter;
    }
}
