package app.auditranscribe.signal;

import app.auditranscribe.utils.MathUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignalHelpersTest {
    @Test
    void computeAlpha() {
        assertEquals(0.6, SignalHelpers.computeAlpha(1));
        assertEquals(
                MathUtils.round(1. / 3., 6),
                MathUtils.round(SignalHelpers.computeAlpha(2), 6)
        );
    }
}