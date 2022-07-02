/*
 * BPMEstimator.java
 *
 * Created on 2022-06-02
 * Updated on 2022-07-02
 *
 * Description: Class that handles all the methods to estimate the Beats Per Minute (BPM) of an
 *              audio signal.
 */

package site.overwrite.auditranscribe.bpm_estimation;

import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that handles all the methods to estimate the Beats Per Minute (BPM) of an audio signal.
 */
public final class BPMEstimator {
    // Constants
    public static final int MAX_SAMPLES_TO_CONSIDER = 1_323_000;  // 30 seconds of samples at 44100 Hz

    public static final int HOP_LENGTH = 512;
    public static final double START_BPM = 120;
    public static final double STD_BPM = 1.;
    public static final double AC_SIZE = 8.;
    public static final double MAX_BPM = 320;

    private BPMEstimator() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Estimate the tempo (beats per minute).
     *
     * @param x          The audio signal.
     * @param sampleRate The sample rate of the audio signal.
     * @return Estimated tempo(s) (beats per minute).
     * @see <a href="https://librosa.org/doc/0.9.1/generated/librosa.beat.tempo.html">Librosa's
     * Implementation</a> of the BPM estimator.
     */
    public static List<Double> estimate(double[] x, double sampleRate) {
        // Limit the number of samples to avoid heap space error
        double[] xTruncated = new double[Math.min(x.length, MAX_SAMPLES_TO_CONSIDER)];
        System.arraycopy(x, 0, xTruncated, 0, xTruncated.length);

        // Generate the window length
        int winLength = UnitConversionUtils.timeToFrames(new double[]{AC_SIZE}, sampleRate, HOP_LENGTH)[0];

        // Generate the tempogram
        double[][] tempogram = Rhythm.tempogram(xTruncated, sampleRate, HOP_LENGTH, winLength);

        // Aggregate the tempogram using the mean
        double[] tempogramAggregated = new double[tempogram.length];
        for (int i = 0; i < tempogram.length; i++) {
            tempogramAggregated[i] = MathUtils.mean(tempogram[i]);
        }

        // Get the BPM values for each bin, skipping the 0-lag bin
        // (Note that this is a DECREASING array)
        double[] bpms = BPMEstimationHelpers.tempoFrequencies(tempogram.length, HOP_LENGTH, sampleRate);

        // Weight the autocorrelation by a log-normal distribution
        double[] logPrior = new double[bpms.length];

        for (int i = 0; i < bpms.length; i++) {
            double temp = (MathUtils.log2(bpms[i]) - MathUtils.log2(START_BPM)) / STD_BPM;
            logPrior[i] = -0.5 * temp * temp;  // We do this because `Math.Pow` is slow
        }

        // Kill everything above the max tempo
        for (int i = 0; i < bpms.length; i++) {
            if (bpms[i] > MAX_BPM) {
                logPrior[i] = Double.NEGATIVE_INFINITY;
            }
        }

        // Weight the tempogram by the prior to get the 'period'
        // (Use `log1p` instead of `log` for numerical stability)
        double[] periods = new double[tempogram.length];
        for (int i = 0; i < tempogram.length; i++) {
            periods[i] = Math.log1p(1e6 * tempogramAggregated[i]) + logPrior[i];
        }

        // Find the maximum period(s) and get the respective BPMs
        double maxPeriod = -Double.MAX_VALUE;
        List<Double> maximumBPMs = new ArrayList<>();

        for (int i = 0; i < periods.length; i++) {
            if (periods[i] >= maxPeriod) {
                maxPeriod = periods[i];
                maximumBPMs.clear();
                maximumBPMs.add(bpms[i]);
            }
        }

        // Return the maximum BPMs
        return maximumBPMs;
    }
}
