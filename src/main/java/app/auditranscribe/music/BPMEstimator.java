/*
 * BPMEstimator.java
 * Description: Handles the estimation of the beats per minute (BPM) of an audio signal.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package app.auditranscribe.music;

import app.auditranscribe.signal.feature_extraction.Tempogram;
import app.auditranscribe.utils.MathUtils;
import app.auditranscribe.utils.StatisticsUtils;
import app.auditranscribe.utils.UnitConversionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the estimation of the beats per minute (BPM) of an audio signal.
 */
public final class BPMEstimator {
    // Constants
    public static final int MAX_SAMPLES_TO_CONSIDER = 661500;  // 15 seconds of samples at 44100 Hz

    public static final int HOP_LENGTH = 512;
    public static final double START_BPM = 120;
    public static final double STD_BPM = 1;
    public static final double AC_SIZE = 8;
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
        double[][] tempogram = Tempogram.tempogram(xTruncated, sampleRate, HOP_LENGTH, winLength);
        int tempogramLength = tempogram.length;

        // Aggregate the tempogram using the mean
        double[] tempogramAggregated = new double[tempogramLength];
        for (int i = 0; i < tempogramLength; i++) {
            tempogramAggregated[i] = StatisticsUtils.mean(tempogram[i]);
        }

        // Get the BPM values for each bin, skipping the 0-lag bin
        // (Note that this is a DECREASING array)
        double[] bpms = new double[tempogramLength];
        bpms[0] = Double.POSITIVE_INFINITY;

        double scale =  60 * sampleRate / HOP_LENGTH;
        for (int i = 1; i < tempogramLength; i++) {
            bpms[i] = scale / i;
        }

        // Weight the autocorrelation by a log-normal distribution
        double[] logPrior = new double[bpms.length];

        for (int i = 0; i < bpms.length; i++) {
            double temp = (MathUtils.log2(bpms[i]) - MathUtils.log2(START_BPM)) / STD_BPM;
            logPrior[i] = -0.5 * temp * temp;  // Multiply by `temp` twice because `Math.pow(temp, 2)` is slow
        }

        // Kill everything above the max tempo
        for (int i = 0; i < bpms.length; i++) {
            if (bpms[i] > MAX_BPM) {
                logPrior[i] = Double.NEGATIVE_INFINITY;
            }
        }

        // Weight the tempogram by the prior to get the 'period'
        // (Use `log1p` instead of `log` for numerical stability)
        double[] periods = new double[tempogramLength];
        for (int i = 0; i < tempogramLength; i++) {
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
