/*
 * STFT.java
 * Description: Class that implements the Short-Time Fourier transform (STFT) algorithm.
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

package app.auditranscribe.signal.representations;

import app.auditranscribe.misc.Complex;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.utils.ArrayUtils;

/**
 * Class that implements the Short-Time Fourier Transform (STFT) algorithm.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Short-time_Fourier_transform">This Wikipedia
 * Article</a> about the STFT.
 */

public final class STFT {
    private STFT() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Computes the Short-Time Fourier Transform (STFT) of the input array <code>x</code>.
     *
     * @param x              The array <code>x</code> representing the data source.
     * @param numFFT         Number of bins to use for the Fast Fourier Transform (FFT).
     * @param hopLength      Number of samples between successive columns.
     * @param windowFunction Signal window function.
     * @return Complex-valued matrix of STFT coefficients.
     */
    public static Complex[][] stft(double[] x, int numFFT, int hopLength, SignalWindow windowFunction) {
        // Get the FFT window
        double[] fftWindow = windowFunction.window.generateWindow(numFFT, false);

        // Pad the window out to `numFFT` size
        fftWindow = ArrayUtils.padCenter(fftWindow, numFFT);

        // Pad the time series so that frames are centered
        double[] xHat = ArrayUtils.padCenter(x, x.length + numFFT);

        // Window the time series
        double[][] xFrames = ArrayUtils.frameVertical(xHat, numFFT, hopLength);
        int innerArrayLength = xFrames[0].length;

        // Now generate the windowed frames
        Complex[][] windowedFrames = new Complex[numFFT][innerArrayLength];
        for (int i = 0; i < numFFT; i++) {
            for (int j = 0; j < innerArrayLength; j++) {
                windowedFrames[i][j] = new Complex(xFrames[i][j] * fftWindow[i]);
            }
        }

        // Transpose the windowed frames
        Complex[][] windowedFramesTransposed = ArrayUtils.transpose(windowedFrames);

        // Generate the transposed STFT matrix
        Complex[][] stftMatrixTransposed = new Complex[innerArrayLength][1 + numFFT / 2];
        for (int i = 0; i < innerArrayLength; i++) {
            stftMatrixTransposed[i] = FFT.rfft(windowedFramesTransposed[i]);
        }

        // Transpose the transposed matrix and return it
        return ArrayUtils.transpose(stftMatrixTransposed);
    }

    /**
     * Computes the Short-Time Fourier Transform (STFT) of the input array <code>x</code>.<br>
     * Returns only the magnitudes of the STFT matrix.
     *
     * @param x              The array <code>x</code> representing the data source.
     * @param numFFT         Number of bins to use for the Fast Fourier Transform (FFT).
     * @param hopLength      Number of samples between successive columns.
     * @param windowFunction Signal window function.
     * @return Real-valued matrix of magnitudes of the STFT coefficients.
     */
    public static double[][] stftMags(double[] x, int numFFT, int hopLength, SignalWindow windowFunction) {
        // Generate the STFT spectrogram
        Complex[][] stft = STFT.stft(x, numFFT, hopLength, windowFunction);

        // Obtain only the magnitudes
        double[][] S = new double[stft.length][];

        for (int i = 0; i < stft.length; i++) {
            S[i] = new double[stft[i].length];
            for (int j = 0; j < stft[i].length; j++) {
                S[i][j] = stft[i][j].abs();
            }
        }

        return S;
    }
}