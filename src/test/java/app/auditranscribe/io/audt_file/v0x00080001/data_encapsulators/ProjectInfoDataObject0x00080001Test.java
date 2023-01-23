package app.auditranscribe.io.audt_file.v0x00080001.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.ProjectInfoDataObject;
import app.auditranscribe.utils.MathUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectInfoDataObject0x00080001Test {
    // Attributes
    String projectName1 = "First Project Name";
    String projectName2 = "Second Project Name";

    int musicKeyIndex1 = 1;
    int musicKeyIndex2 = 8;

    int timeSignatureIndex1 = 0;
    int timeSignatureIndex2 = 7;

    double bpm1 = 123.456;
    double bpm2 = 78.9;

    double offsetSeconds1 = 1.23;
    double offsetSeconds2 = -4.56;

    double playbackVolume1 = 0.1234;
    double playbackVolume2 = 0.56789;

    int currTimeInMS1 = 123456789;
    int currTimeInMS2 = 987654321;

    // Tests
    @Test
    void numBytesNeeded() {
        // Define the two data objects to test number of bytes needed
        ProjectInfoDataObject one = new ProjectInfoDataObject0x00080001(
                projectName1, musicKeyIndex1, timeSignatureIndex1, bpm1, offsetSeconds1, playbackVolume1, currTimeInMS1
        );
        ProjectInfoDataObject two = new ProjectInfoDataObject0x00080001(
                projectName2, musicKeyIndex2, timeSignatureIndex2, bpm2, offsetSeconds2, playbackVolume2, currTimeInMS2
        );

        // Tests
        assertEquals(66, one.numBytesNeeded());
        assertEquals(67, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        ProjectInfoDataObject temp = new ProjectInfoDataObject0x00080001(
                projectName1, musicKeyIndex1, timeSignatureIndex1, bpm1, offsetSeconds1, playbackVolume1, currTimeInMS1
        );

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data attributes from
        String[] projectNames = {projectName1, projectName2};
        int[] musicKeyIndices = {musicKeyIndex1, musicKeyIndex2};
        int[] timeSignatureIndices = {timeSignatureIndex1, timeSignatureIndex2};
        double[] bpms = {bpm1, bpm2};
        double[] offsetSeconds = {offsetSeconds1, offsetSeconds2};
        double[] playbackVolumes = {playbackVolume1, playbackVolume2};
        int[] currTimeInMSs = {currTimeInMS1, currTimeInMS2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 7);  // 7 data attributes
        for (int[] indices1 : indexProduct) {
            ProjectInfoDataObject one = new ProjectInfoDataObject0x00080001(
                    projectNames[indices1[0]],
                    musicKeyIndices[indices1[1]],
                    timeSignatureIndices[indices1[2]],
                    bpms[indices1[3]],
                    offsetSeconds[indices1[4]],
                    playbackVolumes[indices1[5]],
                    currTimeInMSs[indices1[6]]
            );

            for (int[] indices2 : indexProduct) {
                ProjectInfoDataObject two = new ProjectInfoDataObject0x00080001(
                        projectNames[indices2[0]],
                        musicKeyIndices[indices2[1]],
                        timeSignatureIndices[indices2[2]],
                        bpms[indices2[3]],
                        offsetSeconds[indices2[4]],
                        playbackVolumes[indices2[5]],
                        currTimeInMSs[indices2[6]]
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

    @Test
    void testHashCode() {
        // Define the two data objects to test hash code
        ProjectInfoDataObject one = new ProjectInfoDataObject0x00080001(
                projectName1, musicKeyIndex1, timeSignatureIndex1, bpm1, offsetSeconds1, playbackVolume1, currTimeInMS1
        );
        ProjectInfoDataObject two = new ProjectInfoDataObject0x00080001(
                projectName2, musicKeyIndex2, timeSignatureIndex2, bpm2, offsetSeconds2, playbackVolume2, currTimeInMS2
        );

        // Tests
        assertEquals(1701340825, one.hashCode());
        assertEquals(934186417, two.hashCode());
    }
}