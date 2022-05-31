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