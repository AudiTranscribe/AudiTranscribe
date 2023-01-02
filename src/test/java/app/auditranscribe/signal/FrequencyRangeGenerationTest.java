package app.auditranscribe.signal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FrequencyRangeGenerationTest {
    @Test
    void fftFreqBins() {
        // Get the generated frequency bins
        double[] freqBins1 = FrequencyRangeGeneration.fftFreqBins(16, 22050);
        double[] freqBins2 = FrequencyRangeGeneration.fftFreqBins(25, 44100);
        double[] freqBins3 = FrequencyRangeGeneration.fftFreqBins(20, 11025);

        // Define correct frequency bins
        double[] correctFreqBins1 = {
                0., 1378.125, 2756.25, 4134.375, 5512.5, 6890.625, 8268.75, 9646.875, 11025.
        };
        double[] correctFreqBins2 = {
                0., 1764., 3528., 5292., 7056., 8820., 10584., 12348., 14112., 15876., 17640., 19404., 21168.
        };
        double[] correctFreqBins3 = {
                0., 551.25, 1102.5, 1653.75, 2205., 2756.25, 3307.5, 3858.75, 4410., 4961.25, 5512.5
        };

        // Assertions
        assertArrayEquals(correctFreqBins1, freqBins1, 1e-5);
        assertArrayEquals(correctFreqBins2, freqBins2, 1e-5);
        assertArrayEquals(correctFreqBins3, freqBins3, 1e-5);
    }
}