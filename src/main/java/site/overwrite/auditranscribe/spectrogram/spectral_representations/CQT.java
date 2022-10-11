/*
 * CQT.java
 * Description: Class that implements the Constant Q-Transform (CQT) algorithm.
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

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.generic.exceptions.ValueException;
import site.overwrite.auditranscribe.misc.Complex;

/**
 * CQT class that contains Constant-Q Transform (CQT) methods.
 *
 * @implNote Adapted largely from
 * <a href="http://librosa.org/doc/main/_modules/librosa/core/constantq.html#cqt">Librosa's
 * Implementation</a> of the CQT.
 */
public final class CQT {
    private CQT() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Constant-Q Transform function.
     *
     * @param y              Audio time series.
     * @param sr             Sample rate of the audio.
     * @param hopLength      Number of samples between successive CQT columns.
     * @param fmin           Minimum frequency.
     * @param numBins        Number of frequency bins, starting at <code>fmin</code>.
     * @param binsPerOctave  Number of bins per octave.
     * @param tuning         Tuning offset in fractions of a bin.<br>
     *                       If <code>tuning = Double.NaN</code>, the <code>tuning</code> value will
     *                       be automatically estimated from the signal.<br>
     *                       The minimum frequency of the resulting CQT will be modified to
     *                       <code>fmin * Math.pow(2., tuning / binsPerOctave)</code>.
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
            double tuning, WindowFunction windowFunction, CustomTask<?> task
    ) {
        // CQT is the special case of VQT where `gamma` is zero
        return VQT.vqt(
                y, sr, hopLength, fmin, numBins, binsPerOctave, tuning, 0, windowFunction, task, true
        );
    }
}
