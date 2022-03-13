/*
 * Helpers.java
 *
 * Created on 2022-03-07
 * Updated on 2022-03-13
 *
 * Description: Helper methods for the spectral representation functions.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

public class Helpers {
    /**
     * Computes the alpha coefficient.
     *
     * @param binsPerOctave Number of frequency bins per octave.
     * @return The alpha coefficient.
     * @implNote Implementation of this method is from the paper by Glasberg, Brian R., and Brian
     * CJ Moore. "Derivation of auditory filter shapes from notched-noise data". Hearing research
     * 47.1-2 (1990): 103-138.
     */
    public static double computeAlpha(double binsPerOctave) {
        double r = Math.pow(2, 1 / binsPerOctave);
        return (Math.pow(r, 2) - 1) / (Math.pow(r, 2) + 1);
    }
}
