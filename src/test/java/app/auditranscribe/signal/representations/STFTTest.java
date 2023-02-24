package app.auditranscribe.signal.representations;

import app.auditranscribe.misc.Complex;
import app.auditranscribe.signal.windowing.SignalWindow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class STFTTest {
    // Define the arrays
    static double[] array1 = {1, 2, -3, -4, 5, 6, -7, -8};
    static double[] array2 = {
            25.442832341330558, 67.35970565394922, 35.09989960920626, 61.77920972475006,
            62.4223934582534, 34.63818528787935, 10.867667057159514, 19.66065078483943,
            68.66050464419456, 98.64623871074612, 20.397346011908567, 33.125277868376244,
            13.969015082960123, 58.838543552236466, 27.565901473219746, 81.41318148048603
    };

    // Generate the STFT output
    static Complex[][] stftArray1Ones = STFT.stft(array1, 4, 3, SignalWindow.ONES_WINDOW);
    static Complex[][] stftArray1Hann = STFT.stft(array1, 4, 3, SignalWindow.HANN_WINDOW);

    static Complex[][] stftArray2Ones = STFT.stft(array2, 8, 9, SignalWindow.ONES_WINDOW);
    static Complex[][] stftArray2Hann = STFT.stft(array2, 8, 9, SignalWindow.HANN_WINDOW);

    @Test
    void stft() {
        // Check the output
        assertEquals(new Complex(3), stftArray1Ones[0][0].round(3));
        assertEquals(new Complex(0), stftArray1Ones[0][1].round(3));
        assertEquals(new Complex(-4), stftArray1Ones[0][2].round(3));
        assertEquals(new Complex(-1, 2), stftArray1Ones[1][0].round(3));
        assertEquals(new Complex(6, 8), stftArray1Ones[1][1].round(3));
        assertEquals(new Complex(12, -14), stftArray1Ones[1][2].round(3));
        assertEquals(new Complex(-1), stftArray1Ones[2][0].round(3));
        assertEquals(new Complex(-4), stftArray1Ones[2][1].round(3));
        assertEquals(new Complex(0), stftArray1Ones[2][2].round(3));

        assertEquals(new Complex(2), stftArray1Hann[0][0].round(3));
        assertEquals(new Complex(-3), stftArray1Hann[0][1].round(3));
        assertEquals(new Complex(-8), stftArray1Hann[0][2].round(3));
        assertEquals(new Complex(-1, 1), stftArray1Hann[1][0].round(3));
        assertEquals(new Complex(4, 4), stftArray1Hann[1][1].round(3));
        assertEquals(new Complex(7, -7), stftArray1Hann[1][2].round(3));
        assertEquals(new Complex(0), stftArray1Hann[2][0].round(3));
        assertEquals(new Complex(-5), stftArray1Hann[2][1].round(3));
        assertEquals(new Complex(-6), stftArray1Hann[2][2].round(3));

        assertEquals(new Complex(189.6816473), stftArray2Ones[0][0].round(7));
        assertEquals(new Complex(299.9648854), stftArray2Ones[0][1].round(7));
        assertEquals(new Complex(-29.3888389, 126.4149024), stftArray2Ones[1][0].round(7));
        assertEquals(new Complex(-109.4192772, -18.4695954), stftArray2Ones[1][1].round(7));
        assertEquals(new Complex(-9.6570673, -5.5804959), stftArray2Ones[2][0].round(7));
        assertEquals(new Complex(80.4984953, 51.3645067), stftArray2Ones[2][1].round(7));
        assertEquals(new Complex(-21.4968258, 56.2151032), stftArray2Ones[3][0].round(7));
        assertEquals(new Complex(-18.5968297, -45.3988496), stftArray2Ones[3][1].round(7));
        assertEquals(new Complex(-68.5961834), stftArray2Ones[4][0].round(7));
        assertEquals(new Complex(72.1758199), stftArray2Ones[4][1].round(7));

        assertEquals(new Complex(109.5352431), stftArray2Hann[0][0].round(7));
        assertEquals(new Complex(204.6920813), stftArray2Hann[0][1].round(7));
        assertEquals(new Complex(-59.7005644, 64.6025752), stftArray2Hann[1][0].round(7));
        assertEquals(new Complex(-149.8254838, -22.0759244), stftArray2Hann[1][1].round(7));
        assertEquals(new Complex(7.8928825, -48.4477494), stftArray2Hann[2][0].round(7));
        assertEquals(new Complex(72.2532744, 41.6493646), stftArray2Hann[2][1].round(7));
        assertEquals(new Complex(8.8148998, 29.5026756), stftArray2Hann[3][0].round(7));
        assertEquals(new Complex(-47.4669936, -35.5405515), stftArray2Hann[3][1].round(7));
        assertEquals(new Complex(-23.5496788), stftArray2Hann[4][0].round(7));
        assertEquals(new Complex(45.3863248), stftArray2Hann[4][1].round(7));
    }

    @Test
    void istft() {
        // Define expected outputs
        double[] expectedArray1Ones = {1., 2., -3., -4., 5., 6.};
        double[] expectedArray1Hann = {1., 2., -3., -4., 5., 6.};

        double[] expectedArray2Ones = {
                25.44283234, 67.35970565, 35.09989961, 61.77920972,
                0., 34.63818529, 10.86766706, 19.66065078,
                68.66050464
        };
        double[] expectedArray2Hann = {
                25.44283234, 67.35970565, 35.09989961, 61.77920972,
                0., 0., 10.86766706, 19.66065078,
                68.66050464
        };

        // Generate the ISTFT arrays
        double[] istftArray1Ones = STFT.istft(stftArray1Ones, 4, 3, SignalWindow.ONES_WINDOW);
        double[] istftArray1Hann = STFT.istft(stftArray1Hann, 4, 3, SignalWindow.HANN_WINDOW);

        double[] istftArray2Ones = STFT.istft(stftArray2Ones, 8, 9, SignalWindow.ONES_WINDOW);
        double[] istftArray2Hann = STFT.istft(stftArray2Hann, 8, 9, SignalWindow.HANN_WINDOW);

        // Check generated values
        assertEquals(expectedArray1Ones.length, istftArray1Ones.length);
        assertEquals(expectedArray1Hann.length, istftArray1Hann.length);
        assertEquals(expectedArray2Ones.length, istftArray2Ones.length);
        assertEquals(expectedArray2Hann.length, istftArray2Hann.length);

        for (int i = 0; i < expectedArray1Ones.length; i++) {
            assertEquals(expectedArray1Ones[i], istftArray1Ones[i], 1e-5, "Mismatch at index " + i);
        }
        for (int i = 0; i < expectedArray1Hann.length; i++) {
            assertEquals(expectedArray1Hann[i], istftArray1Hann[i], 1e-5, "Mismatch at index " + i);
        }
        for (int i = 0; i < expectedArray2Ones.length; i++) {
            assertEquals(expectedArray2Ones[i], istftArray2Ones[i], 1e-5, "Mismatch at index " + i);
        }
        for (int i = 0; i < expectedArray2Hann.length; i++) {
            assertEquals(expectedArray2Hann[i], istftArray2Hann[i], 1e-5, "Mismatch at index " + i);
        }
    }
}