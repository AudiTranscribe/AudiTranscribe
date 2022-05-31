/*
 * MelSpectrogramTest.java
 *
 * Created on 2022-05-31
 * Updated on 2022-06-01
 *
 * Description: Test `MelSpectrogram.java`.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MelSpectrogramTest {

    @Test
    void melSpectrogram() {
        // Define input arrays
        double[] array1 = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] array2 = {0.1, 0.5, 0.9, 0.123, 0.142, 0.12414, 0.414124, 3.14159, 5.32, 3, 1, 2, 3, 4, 5, 0.123456};

        // Generate mel spectrograms
        double[][] resultant1a = MelSpectrogram.melSpectrogram(array1, 16);
        double[][] resultant1b = MelSpectrogram.melSpectrogram(array1, 16, 2048, 512);

        double[][] resultant2 = MelSpectrogram.melSpectrogram(array2, 32, 16, 64);

        // Check if the corresponding resultant matrices are the same
        assertArrayEquals(resultant1a, resultant1b);

        // Check specific values of the resultant matrices
        assertEquals(165412.3756094377, resultant1a[0][0], 1e-5);
        assertEquals(2188.0735817533787, resultant1a[123][0], 1e-5);
        assertEquals(2054.3504760000196, resultant1a[127][0], 1e-5);

        assertEquals(0, resultant2[0][0], 1e-5);
        assertEquals(0, resultant2[12][0], 1e-5);
        assertEquals(13.461639891604593, resultant2[15][0], 1e-5);
        assertEquals(2.270700514489166, resultant2[32][0], 1e-5);
        assertEquals(0, resultant2[42][0], 1e-5);
        assertEquals(1.6366377853365996, resultant2[96][0], 1e-5);
        assertEquals(0, resultant2[127][0], 1e-5);
    }

    @Test
    void melFilters() {
        // Generate mel filters
        double[][] melFilters = MelSpectrogram.melFilters(22050, 2048);

        // Check specific values of the filters
        assertEquals(0.016182853, melFilters[0][1], 1e-5);
        assertEquals(0.012807235, melFilters[0][4], 1e-5);
        assertEquals(0, melFilters[12][12], 1e-5);
        assertEquals(0.016357256, melFilters[64][186], 1e-5);
        assertEquals(0.000651301, melFilters[127][1019], 1e-5);
        assertEquals(0.0074063656, melFilters[73][234], 1e-5);
    }
}