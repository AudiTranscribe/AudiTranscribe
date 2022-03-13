/*
 * HelpersTest.java
 *
 * Created on 2022-03-09
 * Updated on 2022-03-13
 *
 * Description: Test `Helpers.java`.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.utils.OtherMath;

import static org.junit.jupiter.api.Assertions.*;

class HelpersTest {
    @Test
    void computeAlpha() {
        assertEquals(0.6, Helpers.computeAlpha(1));
        assertEquals(OtherMath.round((double) 1 / 3, 6), OtherMath.round(Helpers.computeAlpha(2), 6));
    }
}