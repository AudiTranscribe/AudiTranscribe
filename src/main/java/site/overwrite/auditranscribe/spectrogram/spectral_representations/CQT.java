/*
 * CQT.java
 *
 * Created on 2022-03-15
 * Updated on 2022-03-15
 *
 * Description: Class that implements the Constant Q-Transform (CQT) algorithm.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.audio.Window;
import site.overwrite.auditranscribe.utils.Complex;

import java.security.InvalidParameterException;

/**
 * CQT class that contains Constant-Q Transform methods.
 *
 * @implNote Adapted largely from
 * <a href="http://librosa.org/doc/main/_modules/librosa/core/constantq.html#cqt">Librosa's
 * Implementation</a> of the CQT.
 */
public class CQT {
    /**
     * Constant-Q Transform function.
     *
     * @param y             Audio time series.
     * @param sr            Sample rate of the audio.
     * @param hopLength     Number of samples between successive CQT columns.
     * @param fmin          Minimum frequency.
     * @param numBins       Number of frequency bins, starting at <code>fmin</code>.
     * @param binsPerOctave Number of bins per octave.
     * @param window        Window specification for the basis filters.
     * @return Constant-Q value each frequency at each time.
     * @throws InvalidParameterException If number of frequency bins is negative or zero.
     * @throws InvalidParameterException If number of bins per octave is negative or zero.
     * @throws InvalidParameterException If number of bins is not a multiple of the number of bins
     *                                   per octave.
     * @throws InvalidParameterException If the current number of frequency bins results in the
     *                                   highest frequency exceeding the Nyquist frequency.
     */
    public static Complex[][] cqt(
            double[] y, double sr, int hopLength, double fmin, int numBins, int binsPerOctave, Window window
    ) {
        // CQT is the special case of VQT with gamma = 0
        return VQT.vqt(y, sr, hopLength, fmin, numBins, binsPerOctave, true, 0, window);
    }
}
