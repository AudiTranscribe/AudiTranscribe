/*
 * CQT.java
 *
 * Created on 2022-03-15
 * Updated on 2022-05-26
 *
 * Description: Class that implements the Constant Q-Transform (CQT) algorithm.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.CustomTask;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.exceptions.ValueException;
import site.overwrite.auditranscribe.utils.Complex;

/**
 * CQT class that contains Constant-Q Transform (CQT) methods.
 *
 * @implNote Adapted largely from
 * <a href="http://librosa.org/doc/main/_modules/librosa/core/constantq.html#cqt">Librosa's
 * Implementation</a> of the CQT.
 */
public class CQT {
    /**
     * Constant-Q Transform function.
     *
     * @param y              Audio time series.
     * @param sr             Sample rate of the audio.
     * @param hopLength      Number of samples between successive CQT columns.
     * @param fmin           Minimum frequency.
     * @param numBins        Number of frequency bins, starting at <code>fmin</code>.
     * @param binsPerOctave  Number of bins per octave.
     * @param windowFunction Window specification for the basis filters.
     * @param task           The <code>CustomTask</code> object that is handling the generation.
     *                       Pass in <code>null</code> if no such task is being used.
     * @return Constant-Q value each frequency at each time.
     * @throws ValueException If: <ul>
     *                        <li>
     *                        The number of frequency bins is negative or zero.
     *                        </li>
     *                        <li>
     *                        The number of bins per octave is negative or zero.
     *                        </li>
     *                        <li>
     *                        The number of bins is not a multiple of the number of bins per octave.
     *                        </li>
     *                        <li>
     *                        The current number of frequency bins results in the highest frequency
     *                        exceeding the Nyquist frequency.
     *                        </li>
     *                        </ul>
     */
    public static Complex[][] cqt(
            double[] y, double sr, int hopLength, double fmin, int numBins, int binsPerOctave,
            WindowFunction windowFunction, CustomTask<?> task
    ) {
        // CQT is the special case of VQT with gamma = 0
        return VQT.vqt(y, sr, hopLength, fmin, numBins, binsPerOctave, true, 0, windowFunction, task);
    }
}
