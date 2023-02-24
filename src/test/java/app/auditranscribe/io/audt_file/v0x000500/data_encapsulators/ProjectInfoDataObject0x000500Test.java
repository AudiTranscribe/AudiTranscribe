package app.auditranscribe.io.audt_file.v0x000500.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.ProjectInfoDataObject;
import app.auditranscribe.utils.MathUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectInfoDataObject0x000500Test {
    static int musicKeyIndex1 = 1;
    static int musicKeyIndex2 = 8;

    static int timeSignatureIndex1 = 0;
    static int timeSignatureIndex2 = 7;

    static double bpm1 = 123.456;
    static double bpm2 = 78.9;

    static double offsetSeconds1 = 1.23;
    static double offsetSeconds2 = -4.56;

    static double playbackVolume1 = 0.1234;
    static double playbackVolume2 = 0.56789;

    static int currTimeInMS1 = 123456789;
    static int currTimeInMS2 = 987654321;

    @Test
    void numBytesNeeded() {
        // Define the two data objects to test number of bytes needed
        ProjectInfoDataObject one = new ProjectInfoDataObject0x000500(
                musicKeyIndex1, timeSignatureIndex1, bpm1, offsetSeconds1, playbackVolume1, currTimeInMS1
        );
        ProjectInfoDataObject two = new ProjectInfoDataObject0x000500(
                musicKeyIndex2, timeSignatureIndex2, bpm2, offsetSeconds2, playbackVolume2, currTimeInMS2
        );

        // Tests
        assertEquals(44, one.numBytesNeeded());
        assertEquals(44, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        ProjectInfoDataObject temp = new ProjectInfoDataObject0x000500(
                musicKeyIndex1, timeSignatureIndex1, bpm1, offsetSeconds1, playbackVolume1, currTimeInMS1
        );

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data attributes from
        int[] musicKeyIndices = {musicKeyIndex1, musicKeyIndex2};
        int[] timeSignatureIndices = {timeSignatureIndex1, timeSignatureIndex2};
        double[] bpms = {bpm1, bpm2};
        double[] offsetSeconds = {offsetSeconds1, offsetSeconds2};
        double[] playbackVolumes = {playbackVolume1, playbackVolume2};
        int[] currTimeInMSs = {currTimeInMS1, currTimeInMS2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 6);  // 6 data attributes
        for (int[] indices1 : indexProduct) {
            ProjectInfoDataObject one = new ProjectInfoDataObject0x000500(
                    musicKeyIndices[indices1[0]],
                    timeSignatureIndices[indices1[1]],
                    bpms[indices1[2]],
                    offsetSeconds[indices1[3]],
                    playbackVolumes[indices1[4]],
                    currTimeInMSs[indices1[5]]
            );

            for (int[] indices2 : indexProduct) {
                ProjectInfoDataObject two = new ProjectInfoDataObject0x000500(
                        musicKeyIndices[indices2[0]],
                        timeSignatureIndices[indices2[1]],
                        bpms[indices2[2]],
                        offsetSeconds[indices2[3]],
                        playbackVolumes[indices2[4]],
                        currTimeInMSs[indices2[5]]
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