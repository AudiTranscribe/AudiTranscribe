/*
 * FilterData.java
 *
 * Created on 2022-03-07
 * Updated on 2022-03-15
 *
 * Description: Class that encapsulates the data for each resampling filter.
 */

package site.overwrite.auditranscribe.audio.filters;

/**
 * Class that contains the resampling filter data.
 */
public class FilterData {
    public double[] halfWin;
    public int precision;
    public double rolloff;
}
