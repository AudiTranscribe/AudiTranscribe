/*
 * Filter.java
 *
 * Created on 2022-03-10
 * Updated on 2022-06-23
 *
 * Description: Enum that contains resampling filters to be used during signal processing.
 */

package site.overwrite.auditranscribe.audio;

import site.overwrite.auditranscribe.audio.filters.AbstractFilter;
import site.overwrite.auditranscribe.audio.filters.KaiserBest;
import site.overwrite.auditranscribe.audio.filters.KaiserFast;

/**
 * Enum that contains resampling filters to be used during signal processing.
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
