package app.auditranscribe.io.audt_file.v0x00080001.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.AudioDataObject;
import app.auditranscribe.utils.MathUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioDataObject0x00080001Test {
    // Attributes
    byte[] compressedOriginalMP3Bytes1 = new byte[]{(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a};
    byte[] compressedOriginalMP3Bytes2 = new byte[]{(byte) 0xab, (byte) 0xcd, (byte) 0xef};

    byte[] compressedSlowedMP3Bytes1 = new byte[]{
            (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55,
            (byte) 0x66, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xaa
    };
    byte[] compressedSlowedMP3Bytes2 = new byte[]{
            (byte) 0xfe, (byte) 0xdc, (byte) 0xba, (byte) 0x98, (byte) 0x76, (byte) 0x54
    };

    double sampleRate1 = 44100;
    double sampleRate2 = 22050;

    int totalDurationInMS1 = 1234;
    int totalDurationInMS2 = 5678;

    // Tests
    @Test
    void numBytesNeeded() {
        // Define the two audio data objects to test the number of bytes needed
        AudioDataObject one = new AudioDataObject0x00080001(
                compressedOriginalMP3Bytes1, compressedSlowedMP3Bytes1, sampleRate1, totalDurationInMS1
        );
        AudioDataObject two = new AudioDataObject0x00080001(
                compressedOriginalMP3Bytes2, compressedSlowedMP3Bytes2, sampleRate2, totalDurationInMS2
        );

        // Tests
        assertEquals(43, one.numBytesNeeded());
        assertEquals(37, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        AudioDataObject temp = new AudioDataObject0x00080001(
                compressedOriginalMP3Bytes1, compressedSlowedMP3Bytes1, sampleRate1, totalDurationInMS1
        );

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data attributes from
        byte[][] compressedOriginalMP3BytesArrays = {compressedOriginalMP3Bytes1, compressedOriginalMP3Bytes2};
        byte[][] compressedSlowedMP3BytesArrays = {compressedSlowedMP3Bytes1, compressedSlowedMP3Bytes2};
        double[] sampleRates = {sampleRate1, sampleRate2};
        int[] totalDurationInMSs = {totalDurationInMS1, totalDurationInMS2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 4);  // 4 data attributes
        for (int[] indices1 : indexProduct) {
            AudioDataObject one = new AudioDataObject0x00080001(
                    compressedOriginalMP3BytesArrays[indices1[0]],
                    compressedSlowedMP3BytesArrays[indices1[1]],
                    sampleRates[indices1[2]],
                    totalDurationInMSs[indices1[3]]
            );

            for (int[] indices2 : indexProduct) {
                AudioDataObject two = new AudioDataObject0x00080001(
                        compressedOriginalMP3BytesArrays[indices2[0]],
                        compressedSlowedMP3BytesArrays[indices2[1]],
                        sampleRates[indices2[2]],
                        totalDurationInMSs[indices2[3]]
                );

                // Check equality
                if (indices1 == indices2) {
                    assertEquals(one, two);
                    assertEquals(two, one);
                } else {
                    assertNotEquals(one, two);
                    assertNotEquals(two, one);
                }
            }
        }
    }
}