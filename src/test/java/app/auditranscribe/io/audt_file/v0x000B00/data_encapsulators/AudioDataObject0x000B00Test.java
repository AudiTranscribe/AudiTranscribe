package app.auditranscribe.io.audt_file.v0x000B00.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.AudioDataObject;
import app.auditranscribe.utils.MathUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AudioDataObject0x000B00Test {
    static byte[] mp3Bytes1 = new byte[]{(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a};
    static byte[] mp3Bytes2 = new byte[]{(byte) 0xab, (byte) 0xcd, (byte) 0xef};

    static double sampleRate1 = 44100;
    static double sampleRate2 = 22050;

    static int totalDurationInMS1 = 1234;
    static int totalDurationInMS2 = 5678;

    @Test
    void numBytesNeeded() {
        // Define the two audio data objects to test the number of bytes needed
        AudioDataObject one = new AudioDataObject0x000B00(mp3Bytes1, sampleRate1, totalDurationInMS1);
        AudioDataObject two = new AudioDataObject0x000B00(mp3Bytes2, sampleRate2, totalDurationInMS2);

        // Tests
        assertEquals(29, one.numBytesNeeded());
        assertEquals(27, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        AudioDataObject temp = new AudioDataObject0x000B00(mp3Bytes1, sampleRate1, totalDurationInMS1);

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data attributes from
        byte[][] mp3BytesArrays = {mp3Bytes1, mp3Bytes2};
        double[] sampleRates = {sampleRate1, sampleRate2};
        int[] totalDurationInMSs = {totalDurationInMS1, totalDurationInMS2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 3);  // 3 data attributes
        for (int[] indices1 : indexProduct) {
            AudioDataObject one = new AudioDataObject0x000B00(
                    mp3BytesArrays[indices1[0]],
                    sampleRates[indices1[1]],
                    totalDurationInMSs[indices1[2]]
            );

            for (int[] indices2 : indexProduct) {
                AudioDataObject two = new AudioDataObject0x000B00(
                        mp3BytesArrays[indices2[0]],
                        sampleRates[indices2[1]],
                        totalDurationInMSs[indices2[2]]
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
