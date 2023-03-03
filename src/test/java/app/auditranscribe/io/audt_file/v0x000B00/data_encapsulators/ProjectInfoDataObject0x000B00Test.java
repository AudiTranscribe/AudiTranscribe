package app.auditranscribe.io.audt_file.v0x000B00.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.ProjectInfoDataObject;
import app.auditranscribe.music.MusicKey;
import app.auditranscribe.music.TimeSignature;
import app.auditranscribe.utils.MathUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectInfoDataObject0x000B00Test {
    static String projectName1 = "First Project Name";
    static String projectName2 = "Second Project Name";

    static MusicKey musicKey1 = MusicKey.C_SHARP_MAJOR;
    static MusicKey musicKey2 = MusicKey.G_FLAT_MAJOR;

    static TimeSignature timeSignature1 = TimeSignature.TWO_TWO;
    static TimeSignature timeSignature2 = TimeSignature.SIX_FOUR;

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
        ProjectInfoDataObject one = new ProjectInfoDataObject0x000B00(
                projectName1, musicKey1, timeSignature1, bpm1, offsetSeconds1, playbackVolume1, currTimeInMS1
        );
        ProjectInfoDataObject two = new ProjectInfoDataObject0x000B00(
                projectName2, musicKey2, timeSignature2, bpm2, offsetSeconds2, playbackVolume2, currTimeInMS2
        );

        // Tests
        assertEquals(64, one.numBytesNeeded());
        assertEquals(65, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        ProjectInfoDataObject temp = new ProjectInfoDataObject0x000B00(
                projectName1, musicKey1, timeSignature1, bpm1, offsetSeconds1, playbackVolume1, currTimeInMS1
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
        MusicKey[] musicKeys = {musicKey1, musicKey2};
        TimeSignature[] timeSignatures = {timeSignature1, timeSignature2};
        double[] bpms = {bpm1, bpm2};
        double[] offsetSeconds = {offsetSeconds1, offsetSeconds2};
        double[] playbackVolumes = {playbackVolume1, playbackVolume2};
        int[] currTimeInMSs = {currTimeInMS1, currTimeInMS2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 7);  // 7 data attributes
        for (int[] indices1 : indexProduct) {
            ProjectInfoDataObject one = new ProjectInfoDataObject0x000B00(
                    projectNames[indices1[0]],
                    musicKeys[indices1[1]],
                    timeSignatures[indices1[2]],
                    bpms[indices1[3]],
                    offsetSeconds[indices1[4]],
                    playbackVolumes[indices1[5]],
                    currTimeInMSs[indices1[6]]
            );

            for (int[] indices2 : indexProduct) {
                ProjectInfoDataObject two = new ProjectInfoDataObject0x000B00(
                        projectNames[indices2[0]],
                        musicKeys[indices2[1]],
                        timeSignatures[indices2[2]],
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
}