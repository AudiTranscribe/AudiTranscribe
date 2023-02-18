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
    // Constants
    public static int MAX_MEM_SIZE = 262144;  // In bytes; maximum number of bytes that can be used

    private STFT() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Computes the Short-Time Fourier Transform (STFT) of the input signal <code>x</code>.<br>
     * <b>Warning</b>: If <code>hopLength</code> is <b>not</b> a factor of <code>x.length</code>,
     * then {@link #istft(Complex[][], int, int, SignalWindow)} will <b>not</b> produce a perfect
     * reconstruction of the original data.
     *
     * @param x              The array <code>x</code> representing the data source.
     * @param numFFT         Number of bins to use for the Fast Fourier Transform (FFT).
     * @param hopLength      Number of samples between successive columns.
     * @param windowFunction Signal window function.
     * @return Complex-valued matrix of STFT coefficients.
     * @implNote See
     * <a href="https://librosa.org/doc/main/_modules/librosa/core/spectrum.html#stft">Librosa's
     * implementation</a> of the STFT.
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

        // Generate the STFT matrix
        Complex[][] stftMatrix = new Complex[1 + numFFT / 2][innerArrayLength];
        Complex[] tempWindow = new Complex[numFFT];
        Complex[] tempFFT;

        for (int i = 0; i < innerArrayLength; i++) {
            // Locate the complex array to apply the FFT to
            for (int j = 0; j < numFFT; j++) {
                tempWindow[j] = windowedFrames[j][i];
            }

            // Apply real-valued FFT to the temp array
            tempFFT = FFT.rfft(tempWindow);

            // Move values back into the matrix
            for (int j = 0; j < 1 + numFFT / 2; j++) {
                stftMatrix[j][i] = tempFFT[j];
            }
        }

        return stftMatrix;
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

    /**
     * Computes the Inverse Short-Time Fourier Transform (ISTFT) of the complex matrix provided.<br>
     * <b>Warning</b>: If the STFT was applied to a signal and the <code>hopLength</code> was
     * <b>not</b> a multiple of that original signal's length, then this will not produce a perfect
     * reproduction of the original signal.
     *
     * @param stftMatrix     The STFT matrix to apply the inverse STFT to.
     * @param numFFT         Number of bins to use for the Fast Fourier Transform (FFT).
     * @param hopLength      Number of samples between successive columns.
     * @param windowFunction Signal window function.
     * @return Signal reconstructed from the STFT matrix.
     * @implNote See
     * <a href="https://librosa.org/doc/main/_modules/librosa/core/spectrum.html#istft">Librosa's
     * implementation</a> of the ISTFT.
     * @see <a href="https://www.audiolabs-erlangen.de/resources/MIR/FMP/C2/C2_STFT-Inverse.html">
     * This resource</a> on how ISTFT is implemented in theory and in practice.
     */
    public static double[] istft(
            Complex[][] stftMatrix, int numFFT, int hopLength, SignalWindow windowFunction
    ) {
        int numFrames = stftMatrix[0].length;

        // Get the inverse FFT window and pad
        double[] ifftWindow = windowFunction.window.generateWindow(numFFT, false);
        ifftWindow = ArrayUtils.padCenter(ifftWindow, numFFT);

        // Apply inverse FFT on head frame
        /*
         * First index (say, k) that does not depend on padding satisfies the inequality
         *      k * hopLength - numFFT / 2 >= 0
         * which implies
         *      k >= (numFFT / 2) / hopLength
         */
        int startFrame = (int) Math.ceil((double) (numFFT / 2) / hopLength);
        Complex[] tempFFT = new Complex[numFFT / 2 + 1];
        Complex[] tempWindow;
        double[][] yTemp = new double[numFFT][startFrame];

        for (int i = 0; i < startFrame; i++) {
            // Obtain the values to apply the IRFFT on
            for (int j = 0; j < 1 + numFFT / 2; j++) {
                tempFFT[j] = stftMatrix[j][i];
            }

            // Apply IRFFT to the temp array
            tempWindow = FFT.irfft(tempFFT, numFFT);

            // Move values back the temp matrix
            for (int j = 0; j < numFFT; j++) {
                yTemp[j][i] = tempWindow[j].re * ifftWindow[j];
            }
        }

        // Perform overlap-add on head block
        double[] headBuffer = new double[(startFrame - 1) * hopLength + numFFT];
        overlapAdd(headBuffer, yTemp, hopLength);

        // Define output array
        double[] output = new double[(numFrames - 1) * hopLength + numFFT - 2 * (numFFT / 2)];

        // If `output` is smaller than the head buffer, take everything. If not, trim off the first `numFFT / 2` samples
        // from the head and copy into target buffer
        System.arraycopy(
                headBuffer,
                numFFT / 2,
                output,
                0,
                Math.min(output.length, headBuffer.length - (numFFT / 2))
        );

        // Determine the number of entries that we can process per iteration
        // (note we multiply by 8 because there are 8 bytes per double)
        int entriesPerIteration = Math.max(1, MAX_MEM_SIZE / (stftMatrix.length * stftMatrix[0].length * 8));

        // This offset compensates for any differences between frame alignment and padding truncation
        int offset = startFrame * hopLength - (numFFT / 2);

        // Process the remaining frames
        for (int blockStart = startFrame, frameNum = 0; blockStart < numFrames; blockStart += entriesPerIteration) {
            int blockEnd = Math.min(blockStart + entriesPerIteration, numFrames);
            yTemp = new double[numFFT][blockEnd - blockStart];

            // Apply IRFFT to the block
            for (int i = blockStart; i < blockEnd; i++) {
                // Obtain the values to apply the IRFFT on
                for (int j = 0; j < 1 + numFFT / 2; j++) {
                    tempFFT[j] = stftMatrix[j][i];
                }

                // Apply IRFFT to the temp array
                tempWindow = FFT.irfft(tempFFT, numFFT);

                // Move values back the temp matrix
                for (int j = 0; j < numFFT; j++) {
                    yTemp[j][i - blockStart] = tempWindow[j].re * ifftWindow[j];
                }
            }

            // Overlap-add the ISTFT block starting at the current frame
            headBuffer = new double[output.length - (frameNum * hopLength + offset)];
            overlapAdd(headBuffer, yTemp, hopLength);

            for (int i = 0; i < headBuffer.length; i++) {
                output[i + frameNum * hopLength + offset] += headBuffer[i];
            }

            // Increment frame number
            frameNum += blockEnd - blockStart;
        }

        // Normalize by sum of squared window
        double[] ifftWindowSumTemp = windowSumSquare(windowFunction, numFrames, hopLength, numFFT);
        double[] ifftWindowSum = new double[output.length];
        System.arraycopy(
                ifftWindowSumTemp,
                numFFT / 2,
                ifftWindowSum,
                0,
                Math.min(output.length, ifftWindowSumTemp.length)
        );

        for (int i = 0; i < output.length; i++) {
            if (ifftWindowSum[i] != 0) output[i] /= ifftWindowSum[i];
        }

        return output;
    }

    // Private methods

    /**
     * Helper method that performs the <em>overlap-add</em> operation for the ISTFT.
     *
     * @param y         Pre-allocated output buffer.
     * @param yTemp     Windowed inverse-stft frames.
     * @param hopLength Hop-length of the STFT analysis.
     * @implNote See <a href="https://librosa.org/doc/main/_modules/librosa/core/spectrum.html">
     * Librosa's implementation</a> of this method.
     */
    private static void overlapAdd(double[] y, double[][] yTemp, int hopLength) {
        int numToProcess = yTemp.length;

        for (int frame = 0; frame < yTemp[0].length; frame++) {
            int sample = frame * hopLength;
            if (numToProcess > y.length - sample) numToProcess = y.length - sample;

            for (int i = 0; i < numToProcess; i++) {
                y[i + sample] += yTemp[i][frame];
            }
        }
    }

    /**
     * Compute the sum-square envelope of a window function at a given hop length.
     *
     * @param windowFunction Signal window function.
     * @param numFrames      The number of analysis frames.
     * @param hopLength      The number of samples to advance between frames.
     * @param numFFT         Number of FFT bins.
     * @return The sum-squared envelope of the window function.
     * @implNote See
     * <a href="https://librosa.org/doc/main/_modules/librosa/filters.html#window_sumsquare">
     * Librosa's implementation</a> of this method.
     */
    private static double[] windowSumSquare(
            SignalWindow windowFunction, int numFrames, int hopLength, int numFFT
    ) {
        // Compute the squared window at the desired length
        double[] winSq = windowFunction.window.generateWindow(numFFT, false);
        for (int i = 0; i < numFFT; i++) winSq[i] *= winSq[i];  // Square each value
        winSq = ArrayUtils.padCenter(winSq, numFFT);

        // Fill the envelope
        double[] sumSquare = new double[(numFrames - 1) * hopLength + numFFT];

        for (int i = 0; i < numFrames; i++) {
            int sampleNum = i * hopLength;
            int end = Math.min(sumSquare.length, sampleNum + numFFT);
            for (int j = sampleNum; j < end; j++) {
                sumSquare[j] += winSq[j - sampleNum];
            }
        }

        return sumSquare;
    }
}
