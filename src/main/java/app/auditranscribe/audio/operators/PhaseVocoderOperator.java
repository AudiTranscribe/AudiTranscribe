/*
 * PhaseVocoderOperator.java
 * Description: Phase vocoder operator that operates on audio bytes.
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

package app.auditranscribe.audio.operators;

import app.auditranscribe.misc.CapacityQueue;
import app.auditranscribe.misc.Complex;
import app.auditranscribe.signal.representations.STFT;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.utils.TypeConversionUtils;

/**
 * Phase vocoder operator that operates on audio bytes.
 */
public class PhaseVocoderOperator extends TimeStretchOperator {
    // Constants
    final int PROCESSING_SIZE = 2048;

    // Attributes
    private final int numFFT;
    private final int hopLength;
    private final SignalWindow window;

    private final CapacityQueue<Double> input = new CapacityQueue<>(PROCESSING_SIZE);

    /**
     * Initializes a new phase vocoder.
     *
     * @param stretchFactor Initial time stretch factor.
     * @param numFFT        Number of FFT bins.
     * @param hopLength     Number of samples between successive columns.
     * @param window        Signal window to use when windowing samples.
     */
    public PhaseVocoderOperator(double stretchFactor, int numFFT, int hopLength, SignalWindow window) {
        super(stretchFactor);
        this.numFFT = numFFT;
        this.hopLength = hopLength;
        this.window = window;
    }

    // Public methods
    @Override
    public double[] process() throws InterruptedException {
        // Ensure there is enough data to process
        if (inputBuffer.size() < PROCESSING_SIZE) {
            return new double[0];
        }

        // Obtain the samples
        double[] rawSamples = getNextSamples();

        // Perform STFT on samples
        Complex[][] stftMatrix = STFT.stft(rawSamples, numFFT, hopLength, window);

        // Perform time stretching
        Complex[][] modifiedSTFT = PhaseVocoder.phaseVocoder(stftMatrix, hopLength, 1. / stretchFactor);

        // Obtain modified samples
        return STFT.istft(modifiedSTFT, numFFT, hopLength, window);
    }

    // Private methods

    /**
     * Helper method that obtains the next set of samples to process.
     *
     * @return The next set of raw samples to process.
     * @throws InterruptedException If the taking of new elements was interrupted while waiting.
     */
    private double[] getNextSamples() throws InterruptedException {
        for (int i = 0; i < PROCESSING_SIZE; i++) {
            input.offer(inputBuffer.take());
        }
        return TypeConversionUtils.toDoubleArray(input.toArray(new Double[0]));
    }
}
