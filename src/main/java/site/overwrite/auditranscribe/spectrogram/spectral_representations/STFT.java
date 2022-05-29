/*
 * STFT.java
 *
 * Created on 2022-03-10
 * Updated on 2022-05-28
 *
 * Description: Class that implements the Short-Time Fourier Transform (STFT) algorithm.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.utils.ArrayUtils;
import site.overwrite.auditranscribe.misc.Complex;

/**
 * Class that implements the Short-Time Fourier Transform (STFT) algorithm.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Short-time_Fourier_transform">This Wikipedia
 * Article</a> about the STFT.
 */
public class STFT {
    // Public methods

    /**
     * Computes the Short-Time Fourier Transform (STFT) of the input array <code>x</code>.
     *
     * @param x              The complex array <code>x</code> representing the data source.
     * @param numFFT         Number of bins to use for the Fast Fourier Transform (FFT).
     * @param hopLength      Number of samples between successive columns.
     * @param windowFunction Windowing function.
     * @return Complex-valued matrix of STFT coefficients.
     */
    public static Complex[][] stft(double[] x, int numFFT, int hopLength, WindowFunction windowFunction) {
        // Get the FFT window
        double[] fftWindow = windowFunction.window.generateWindow(numFFT, false);

        // Pad the window out to `numFFT` size
        fftWindow = ArrayUtils.padCenter(fftWindow, numFFT);

        // Pad the time series so that frames are centered
        double[] xHat = ArrayUtils.padCenter(x, x.length + numFFT);

        // Window the time series
        double[][] xFrames = ArrayUtils.frame(xHat, numFFT, hopLength, true);
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
            stftMatrixTransposed[i] = FFT.fft(windowedFramesTransposed[i]);
        }

        // Transpose the transposed matrix and return it
        return ArrayUtils.transpose(stftMatrixTransposed);
    }
}
