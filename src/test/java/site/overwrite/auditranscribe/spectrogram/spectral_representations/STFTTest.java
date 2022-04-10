/*
 * STFTTest.java
 *
 * Created on 2022-04-10
 * Updated on 2022-04-10
 *
 * Description: Test `STFT.java`
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Window;
import site.overwrite.auditranscribe.utils.Complex;

import static org.junit.jupiter.api.Assertions.*;

class STFTTest {
    @Test
    void stft() {
        // Define the array
        double[] array = {1, 2, -3, -4, 5, 6, -7, -8};

        // Generate the STFT output
        Complex[][] stftArrayOnes = STFT.stft(array, 4, 3, Window.ONES_WINDOW);
        Complex[][] stftArrayHann = STFT.stft(array, 4, 3, Window.HANN_WINDOW);

        // Check the output
        assertEquals(new Complex(2), stftArrayOnes[0][0].roundNicely(3));
        assertEquals(new Complex(0), stftArrayOnes[0][1].roundNicely(3));
        assertEquals(new Complex(-4), stftArrayOnes[0][2].roundNicely(3));
        assertEquals(new Complex(-4), stftArrayOnes[1][0].roundNicely(3));
        assertEquals(new Complex(6, 8), stftArrayOnes[1][1].roundNicely(3));
        assertEquals(new Complex(12,-14), stftArrayOnes[1][2].roundNicely(3));
        assertEquals(new Complex(-6), stftArrayOnes[2][0].roundNicely(3));
        assertEquals(new Complex(-4), stftArrayOnes[2][1].roundNicely(3));
        assertEquals(new Complex(0), stftArrayOnes[2][2].roundNicely(3));

        assertEquals(new Complex(3), stftArrayHann[0][0].roundNicely(3));
        assertEquals(new Complex(-3), stftArrayHann[0][1].roundNicely(3));
        assertEquals(new Complex(-8), stftArrayHann[0][2].roundNicely(3));
        assertEquals(new Complex(-1), stftArrayHann[1][0].roundNicely(3));
        assertEquals(new Complex(4, 4), stftArrayHann[1][1].roundNicely(3));
        assertEquals(new Complex(7, -7), stftArrayHann[1][2].roundNicely(3));
        assertEquals(new Complex(-1), stftArrayHann[2][0].roundNicely(3));
        assertEquals(new Complex(-5), stftArrayHann[2][1].roundNicely(3));
        assertEquals(new Complex(-6), stftArrayHann[2][2].roundNicely(3));
    }
}