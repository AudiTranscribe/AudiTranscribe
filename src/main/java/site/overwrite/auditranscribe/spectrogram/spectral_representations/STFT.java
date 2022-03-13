/*
 * STFT.java
 *
 * Created on 2022-03-10
 * Updated on 2022-03-13
 *
 * Description: Class that implements the Short-Time Fourier Transform (STFT) algorithm.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.audio.Window;
import site.overwrite.auditranscribe.utils.ArrayAdjustment;
import site.overwrite.auditranscribe.utils.Complex;

public class STFT {
    // Public methods
    public static Complex[][] stft(double[] y, int numFFT, int hopLength, Window window) {
        // Get the FFT window
        double[] fftWindow = window.window.generateWindow(numFFT, false);

        // Pad the window out to `numFFT` size
        fftWindow = ArrayAdjustment.padCenter(fftWindow, numFFT);

        // Pad the time series so that frames are centered
        double[] yHat = ArrayAdjustment.padCenter(y, y.length + numFFT);

        // Window the time series
        double[][] yFrames = ArrayAdjustment.frame(yHat, numFFT, hopLength, true);
        int innerArrayLength = yFrames[0].length;

        // Now generate the windowed frames
        Complex[][] windowedFrames = new Complex[numFFT][innerArrayLength];
        for (int i = 0; i < numFFT; i++) {
            for (int j = 0; j < innerArrayLength; j++) {
                windowedFrames[i][j] = new Complex(yFrames[i][j] * fftWindow[i], 0);
            }
        }

        // Transpose the windowed frames
        Complex[][] windowedFramesTransposed = new Complex[innerArrayLength][numFFT];
        for (int i = 0; i < innerArrayLength; i++) {
            for (int j = 0; j < numFFT; j++) {
                windowedFramesTransposed[i][j] = windowedFrames[j][i];
            }
        }

        // Generate the transposed STFT matrix
        Complex[][] stftMatrixTransposed = new Complex[innerArrayLength][1 + numFFT / 2];
        for (int i = 0; i < innerArrayLength; i++) {
            stftMatrixTransposed[i] = FFT.fft(windowedFramesTransposed[i]);
        }

        // Transpose the transposed matrix to get the desired result
        Complex[][] stftMatrix = new Complex[1 + numFFT / 2][innerArrayLength];
        for (int i = 0; i < 1 + numFFT / 2; i++) {
            for (int j = 0; j < innerArrayLength; j++) {
                stftMatrix[i][j] = stftMatrixTransposed[j][i];
            }
        }


        // Return the final STFT matrix
        return stftMatrix;
    }
}
