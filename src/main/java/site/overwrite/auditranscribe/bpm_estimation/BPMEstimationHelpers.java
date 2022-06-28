/*
 * BPMEstimationHelpers.java
 *
 * Created on 2022-05-31
 * Updated on 2022-06-28
 *
 * Description: Class containing helper methods for BPM estimation.
 */

package site.overwrite.auditranscribe.bpm_estimation;

/**
 * Class containing helper methods for BPM estimation.
 */
public final class BPMEstimationHelpers {
    private BPMEstimationHelpers() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that computes the frequencies (in beats per minute) corresponding to an onset
     * auto-correlation or tempogram matrix.
     *
     * @param numBins    The number of lag bins.
     * @param hopLength  The number of samples between each bin.
     * @param sampleRate The audio sampling rate.
     * @return Vector of bin frequencies measured in BPM.
     */
    public static double[] tempoFrequencies(int numBins, double hopLength, double sampleRate) {
        double[] binFreqs = new double[numBins];
        binFreqs[0] = Double.POSITIVE_INFINITY;
        for (int i = 1; i < numBins; i++) {
            binFreqs[i] = 60. * sampleRate / (i * hopLength);
        }
        return binFreqs;
    }
}
