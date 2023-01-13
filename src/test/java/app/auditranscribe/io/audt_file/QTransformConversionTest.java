package app.auditranscribe.io.audt_file;

import app.auditranscribe.generic.tuples.Triple;
import app.auditranscribe.io.audt_file.base.data_encapsulators.QTransformDataObject;
import app.auditranscribe.utils.TypeConversionUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class QTransformConversionTest {
    @Test
    void qTransformMagnitudesToByteData() throws IOException {
        // Define sample Q-transform magnitudes
        double[][] magnitudes = {
                {0.1, -0.2, 0.3},
                {4, -5, 6},
                {7, -8, 9}
        };

        // Define correct return values
        byte[] correctBytes = {
                64, 0, 0, 0, 3, 4, 0, -16, 21, -7, -7, -7, -6, -11, 117, 117, 117, -4, -4, -4,
                -3, 52, -76, -76, -76, -83, 45, 45, 45, 82, -46, -46, -46, 97, -31, -31, -31, -128, 0, 0,
                0, 127, -1, -1, -1
        };
        double correctMinMagnitude = -8;
        double correctMaxMagnitude = 9;

        // Run the method
        Triple<Byte[], Double, Double> returned = QTransformDataObject.qTransformMagnitudesToByteData(
                magnitudes, null
        );

        // Tests
        assertArrayEquals(correctBytes, TypeConversionUtils.toByteArray(returned.value0()));
        assertEquals(correctMinMagnitude, returned.value1(), 1e-5);
        assertEquals(correctMaxMagnitude, returned.value2(), 1e-5);
    }

    @Test
    void byteDataToQTransformMagnitudes() throws IOException {
        // Define sample byte data and the min and max magnitudes
        byte[] bytes = {
                64, 0, 0, 0, 3, 4, 0, -16, 21, -7, -7, -7, -6, -11, 117, 117, 117, -4, -4, -4,
                -3, 52, -76, -76, -76, -83, 45, 45, 45, 82, -46, -46, -46, 97, -31, -31, -31, -128, 0, 0,
                0, 127, -1, -1, -1
        };
        double minMagnitude = -8;
        double maxMagnitude = 9;

        // Define correct magnitude array
        double[][] correctMagnitudes = {
                {0.1, -0.2, 0.3},
                {4, -5, 6},
                {7, -8, 9}
        };

        // Run the method
        double[][] magnitudes = QTransformDataObject.byteDataToQTransformMagnitudes(bytes, minMagnitude, maxMagnitude);

        // Check lengths
        assertEquals(correctMagnitudes.length, magnitudes.length);
        assertEquals(correctMagnitudes[0].length, magnitudes[0].length);

        // Check values
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(correctMagnitudes[i][j], magnitudes[i][j], 1e-5);
            }
        }
    }
}
