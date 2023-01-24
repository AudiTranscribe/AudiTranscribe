package app.auditranscribe.signal;

import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.misc.Complex;
import app.auditranscribe.signal.windowing.SignalWindow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaveletTest {
    @Test
    void computeWaveletLengths() {
        // Define frequency arrays
        double[] freqs1 = {16., 160., 304., 448., 592., 736., 880., 1024.};
        double[] freqs2 = {
                4000., 5066.666666666667, 6133.333333333333, 7200., 8266.666666666667, 9333.333333333333, 10400.,
                11466.666666666667, 12533.333333333333, 13600., 14666.666666666667, 15733.333333333333, 16800.,
                17866.666666666667, 18933.333333333333, 20000.
        };
        double[] freqs3 = {16., 160., 304., 448., 592., 736., 880., 1024.};  // Will use different sample rate for test
        double[] freqs4 = {512.};

        // Define correct wavelet arrays
        double[] waveletLengthCorrect1 = {
                183.85871733836015, 126.0600285850405, 174.76882430647294, 202.74752339773417, 220.9079832122389,
                233.64686912120368, 243.0766661099045, 234.06783758354527
        };
        double freqCutoffCorrect1 = 1156.7200167930532;

        double[] waveletLengthCorrect2 = {
                44.93554785610319, 39.558139534883715, 39.85751710085868, 40.07092110183219, 40.23073569482297,
                40.35489493560581, 40.454133635333974, 40.53527036655823, 40.60284408329107, 40.65999330431871,
                40.70895754335801, 40.75137769012145, 40.78848349736282, 40.821214757202064, 40.85030200531554,
                39.81615042597019
        };
        double freqCutoffCorrect2 = 20827.662743054465;

        double[] waveletLengthCorrect3 = {
                1.8385871733836014, 1.260600285850405, 1.7476882430647296, 2.027475233977342, 2.2090798321223892,
                2.336468691212037, 2.430766661099045, 2.340678375835453
        };
        double freqCutoffCorrect3 = 1156.7200167930532;

        double[] waveletLengthCorrect4 = {4.8404859267353615};
        double freqCutoffCorrect4 = 6642.3043402777785;

        // Compute wavelet lengths
        Pair<double[], Double> waveletLengthsPair1 = Wavelet.computeWaveletLengths(
                freqs1, 44100, SignalWindow.HANN_WINDOW, 1, false, 0, 0
        );
        double[] waveletLengths1 = waveletLengthsPair1.value0();
        double freqCutoff1 = waveletLengthsPair1.value1();

        Pair<double[], Double> waveletLengthsPair2 = Wavelet.computeWaveletLengths(
                freqs2, 44100, SignalWindow.HANN_WINDOW, 1, false, 0, 1.23
        );
        double[] waveletLengths2 = waveletLengthsPair2.value0();
        double freqCutoff2 = waveletLengthsPair2.value1();

        Pair<double[], Double> waveletLengthsPair3 = Wavelet.computeWaveletLengths(
                freqs3, 441, SignalWindow.HANN_WINDOW, 1, false, 0, 0
        );
        double[] waveletLengths3 = waveletLengthsPair3.value0();
        double freqCutoff3 = waveletLengthsPair3.value1();

        Pair<double[], Double> waveletLengthsPair4 = Wavelet.computeWaveletLengths(
                freqs4, 44100, SignalWindow.HANN_WINDOW, 1, false, 0, 12.3
        );
        double[] waveletLengths4 = waveletLengthsPair4.value0();
        double freqCutoff4 = waveletLengthsPair4.value1();

        // Assertions
        assertArrayEquals(waveletLengthCorrect1, waveletLengths1, 1e-10);
        assertEquals(freqCutoffCorrect1, freqCutoff1, 1e-10);

        assertArrayEquals(waveletLengthCorrect2, waveletLengths2, 1e-10);
        assertEquals(freqCutoffCorrect2, freqCutoff2, 1e-10);

        assertArrayEquals(waveletLengthCorrect3, waveletLengths3, 1e-10);
        assertEquals(freqCutoffCorrect3, freqCutoff3, 1e-10);

        assertArrayEquals(waveletLengthCorrect4, waveletLengths4, 1e-10);
        assertEquals(freqCutoffCorrect4, freqCutoff4, 1e-10);
    }

    @Test
    void computeWaveletBasis() {
        // Define frequency arrays
        double[] freqs1 = {16., 160., 304., 448., 592., 736., 880., 1024.};
        double[] freqs2 = {
                4000., 5066.666666666667, 6133.333333333333, 7200., 8266.666666666667, 9333.333333333333, 10400.,
                11466.666666666667, 12533.333333333333, 13600., 14666.666666666667, 15733.333333333333, 16800.,
                17866.666666666667, 18933.333333333333, 20000.
        };
        double[] freqs3 = {16., 160., 304., 448., 592., 736., 880., 1024.};  // `padFFT = false` in the test

        // Define correct wavelet arrays
        Complex[][] correctBasis1 = {
                {
                        Complex.ZERO, new Complex(0.9741291403770447, -0.2259921133518219),
                        Complex.ZERO, Complex.ZERO
                },
                {
                        Complex.ZERO, new Complex(-0.650936484336853, -0.7591322064399719),
                        Complex.ZERO, Complex.ZERO
                },
                {
                        Complex.ZERO, new Complex(-0.3719630539417267, 0.9282475113868713),
                        Complex.ZERO, Complex.ZERO
                },
                {
                        Complex.ZERO, new Complex(0.49751538038253784, -0.04978392273187637),
                        new Complex(0.5), Complex.ZERO
                },
                {
                        Complex.ZERO, new Complex(-0.2742583155632019, -0.41806983947753906),
                        new Complex(0.5), Complex.ZERO
                },
                {
                        Complex.ZERO, new Complex(-0.24380545318126678, 0.43653053045272827),
                        new Complex(0.5), Complex.ZERO
                },
                {
                        Complex.ZERO, new Complex(0.4997970163822174, 0.014245657250285149),
                        new Complex(0.5), Complex.ZERO
                },
                {
                        Complex.ZERO, new Complex(-0.218545064330101, -0.44970884919166565),
                        new Complex(0.5), Complex.ZERO
                },
        };
        double[] correctFilterLengthsArr1 = {
                1.8385871733836014, 1.260600285850405, 1.7476882430647296, 2.027475233977342,
                2.2090798321223892, 2.336468691212037, 2.430766661099045, 2.340678375835453
        };

        Complex[][] correctBasis2 = {
                {new Complex(-0.9396926164627075, -0.3420201539993286)},
                {new Complex(-0.686241626739502, 0.7273736596107483)},
                {new Complex(0.39607977867126465, 0.9182161092758179)},
                {new Complex(1)},
                {new Complex(0.39607977867126465, -0.9182161092758179)},
                {new Complex(-0.686241626739502, -0.7273736596107483)},
                {new Complex(-0.9396926164627075, 0.3420201539993286)},
                {new Complex(-0.05814483016729355, 0.9983081817626953)},
                {new Complex(0.8936326503753662, 0.448799192905426)},
                {new Complex(0.7660444378852844, -0.6427876353263855)},
                {new Complex(-0.2868032455444336, -0.957989513874054)},
                {new Complex(-0.9932383298873901, -0.11609291285276413)},
                {new Complex(-0.5, 0.8660253882408142)},
                {new Complex(0.5971586108207703, 0.8021231889724731)},
                {new Complex(0.9730448722839355, -0.23061586916446686)},
                {new Complex(0.1736481785774231, -0.9848077297210693)}
        };
        double[] correctFilterLengthsArr2 = {
                0.9170519970633304, 0.8073089700996676, 0.8134187163440548, 0.8177739000373918,
                0.8210354223433259, 0.8235692844001187, 0.8255945639864077, 0.8272504156440506,
                0.828629471087573, 0.8297957817207829, 0.8307950519052655, 0.8316607691861522,
                0.8324180305584249, 0.8330860154531033, 0.8336796327615418, 0.8125744984891876
        };

        Complex[][] correctBasis3 = {
                {Complex.ZERO, new Complex(0.9741291403770447, -0.2259921133518219), Complex.ZERO},
                {Complex.ZERO, new Complex(-0.650936484336853, -0.7591322064399719), Complex.ZERO},
                {Complex.ZERO, new Complex(-0.3719630539417267, 0.9282475113868713), Complex.ZERO},
                {Complex.ZERO, new Complex(0.49751538038253784, -0.04978392273187637), new Complex(0.5)},
                {Complex.ZERO, new Complex(-0.2742583155632019, -0.41806983947753906), new Complex(0.5)},
                {Complex.ZERO, new Complex(-0.24380545318126678, 0.43653053045272827), new Complex(0.5)},
                {Complex.ZERO, new Complex(0.4997970163822174, 0.014245657250285149), new Complex(0.5)},
                {Complex.ZERO, new Complex(-0.218545064330101, -0.44970884919166565), new Complex(0.5)},
        };
        double[] correctFilterLengthsArr3 = {
                1.8385871733836014, 1.260600285850405, 1.7476882430647296, 2.027475233977342, 2.2090798321223892,
                2.336468691212037, 2.430766661099045, 2.340678375835453
        };

        // Compute wavelet bases
        Pair<Complex[][], double[]> waveletBasisPair1 = Wavelet.computeWaveletBasis(
                freqs1, 441, SignalWindow.HANN_WINDOW, 1, true, 1, false, 0,
                0
        );
        Complex[][] basis1 = waveletBasisPair1.value0();
        double[] filterLengths1 = waveletBasisPair1.value1();

        Pair<Complex[][], double[]> waveletBasisPair2 = Wavelet.computeWaveletBasis(
                freqs2, 900, SignalWindow.HANN_WINDOW, 1, true, 1, false, 0,
                0
        );
        Complex[][] basis2 = waveletBasisPair2.value0();
        double[] filterLengths2 = waveletBasisPair2.value1();

        Pair<Complex[][], double[]> waveletBasisPair3 = Wavelet.computeWaveletBasis(
                freqs3, 441, SignalWindow.HANN_WINDOW, 1, false, 1, false, 0,
                0
        );
        Complex[][] basis3 = waveletBasisPair3.value0();
        double[] filterLengths3 = waveletBasisPair3.value1();

        // Assertions
        assertEquals(correctBasis1.length, basis1.length, "Basis 1 shape incorrect");
        assertEquals(correctBasis1[0].length, basis1[0].length, "Basis 1 shape incorrect");
        for (int i = 0; i < correctBasis1.length; i++) {
            for (int j = 0; j < correctBasis1[0].length; j++) {
                assertEquals(
                        correctBasis1[i][j].round(5),
                        basis1[i][j].round(5),
                        "Error at position (" + i + ", " + j + ")"
                );
            }
        }
        assertArrayEquals(correctFilterLengthsArr1, filterLengths1, 1e-10);

        assertEquals(correctBasis2.length, basis2.length, "Basis 2 shape incorrect");
        assertEquals(correctBasis2[0].length, basis2[0].length, "Basis 2 shape incorrect");
        for (int i = 0; i < correctBasis2.length; i++) {
            for (int j = 0; j < correctBasis2[0].length; j++) {
                assertEquals(
                        correctBasis2[i][j].round(5),
                        basis2[i][j].round(5),
                        "Error at position (" + i + ", " + j + ")"
                );
            }
        }
        assertArrayEquals(correctFilterLengthsArr2, filterLengths2, 1e-10);

        assertEquals(correctBasis3.length, basis3.length, "Basis 3 shape incorrect");
        assertEquals(correctBasis3[0].length, basis3[0].length, "Basis 3 shape incorrect");
        for (int i = 0; i < correctBasis3.length; i++) {
            for (int j = 0; j < correctBasis3[0].length; j++) {
                assertEquals(
                        correctBasis3[i][j].round(5),
                        basis3[i][j].round(5),
                        "Error at position (" + i + ", " + j + ")"
                );
            }
        }
        assertArrayEquals(correctFilterLengthsArr3, filterLengths3, 1e-10);
    }
}