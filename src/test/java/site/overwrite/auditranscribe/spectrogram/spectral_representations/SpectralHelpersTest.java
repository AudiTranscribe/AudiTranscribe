/*
 * SpectralHelpersTest.java
 *
 * Created on 2022-03-09
 * Updated on 2022-03-13
 *
 * Description: Test `SpectralHelpers.java`.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.utils.MathUtils;

import static org.junit.jupiter.api.Assertions.*;

class SpectralHelpersTest {
    @Test
    void computeAlpha() {
        assertEquals(0.6, SpectralHelpers.computeAlpha(1));
        assertEquals(MathUtils.round((double) 1 / 3, 6), MathUtils.round(SpectralHelpers.computeAlpha(2), 6));
    }
}