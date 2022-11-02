/*
 * ProjectDataTest.java
 * Description: Test `ProjectData.java`.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package site.overwrite.auditranscribe.io.audt_file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import site.overwrite.auditranscribe.io.CompressionHandlers;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.*;
import site.overwrite.auditranscribe.io.audt_file.v0x00050002.data_encapsulators.*;
import site.overwrite.auditranscribe.generic.tuples.Triple;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.TypeConversionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs({OS.LINUX})
class ProjectDataTest {
    // Define helper attributes
    double[][] qTransformMagnitudes1;
    double[][] qTransformMagnitudes2;

    double[] timesToPlaceRectangles1;
    double[] noteDurations1;
    int[] noteNums1;

    double[] timesToPlaceRectangles2;
    double[] noteDurations2;
    int[] noteNums2;

    // Define data to be used within the tests
    QTransformDataObject qTransformDataObject1;
    QTransformDataObject qTransformDataObject2;

    AudioDataObject audioDataObject1;
    AudioDataObject audioDataObject2;

    ProjectInfoDataObject projectInfoDataObject1;
    ProjectInfoDataObject projectInfoDataObject2;

    MusicNotesDataObject musicNotesDataObject1;
    MusicNotesDataObject musicNotesDataObject2;

    UnchangingDataPropertiesObject unchangingDataPropertiesObject1;
    UnchangingDataPropertiesObject unchangingDataPropertiesObject2;

    // Initialization method
    public ProjectDataTest() throws IOException {
        // Define sample array data
        // (These are example arrays, not actual data)
        qTransformMagnitudes1 = new double[][]{
                {65.43, -123.45, 9876.54321, 3.14159265, -0.000082147128481},
                {65.43, 9876.54321, 3.14159265, -0.000082147128481, -123.45},
                {65.43, -123.45, 3.14159265, -0.000082147128481, 9876.54321}
        };
        qTransformMagnitudes2 = new double[][]{
                {325.124, -241.124, 14.142, 214.4224, 91.21, -19431.13, -0.0000001241},
                {-241.124, 325.124, 14.142, 214.4224, -19431.13, -0.0000001241, 91.21},
                {14.142, 325.124, -241.124, 214.4224, 91.21, -0.0000001241, -19431.13},
        };

        timesToPlaceRectangles1 = new double[]{1, 2, 3, 4.5, 6.7, 8.9};
        noteDurations1 = new double[]{0.5, 1, 1.5, 2.5, 3.5, 10};
        noteNums1 = new int[]{32, 41, 91, 82, 84, 55};

        timesToPlaceRectangles2 = new double[]{0, 0.9, 1.2, 1.8, 2.4, 3.3, 3.6, 4.2};
        noteDurations2 = new double[]{0.9, 0.3, 0.6, 0.6, 0.9, 0.3, 0.6, 0.6};
        noteNums2 = new int[]{64, 62, 53, 55, 60, 59, 52, 53};

        // Convert the magnitude data to required form
        Triple<Byte[], Double, Double> conversionTuple1 =
                QTransformDataObject.qTransformMagnitudesToByteData(qTransformMagnitudes1, null);
        byte[] qTransformBytes1 = TypeConversionUtils.toByteArray(conversionTuple1.value0());
        double minMagnitude1 = conversionTuple1.value1();
        double maxMagnitude1 = conversionTuple1.value2();
        Triple<Byte[], Double, Double> conversionTuple2 =
                QTransformDataObject.qTransformMagnitudesToByteData(qTransformMagnitudes2, null);
        byte[] qTransformBytes2 = TypeConversionUtils.toByteArray(conversionTuple2.value0());
        double minMagnitude2 = conversionTuple2.value1();
        double maxMagnitude2 = conversionTuple2.value2();

        // Define data to be used within the tests
        qTransformDataObject1 = new QTransformDataObject0x00050002(
                qTransformBytes1, minMagnitude1, maxMagnitude1
        );
        qTransformDataObject2 = new QTransformDataObject0x00050002(
                qTransformBytes2, minMagnitude2, maxMagnitude2
        );

        audioDataObject1 = new AudioDataObject0x00050002(
                CompressionHandlers.lz4Compress(Files.readAllBytes(Paths.get(
                        IOMethods.getAbsoluteFilePath("testing-files/audio/A440.mp3")
                ))),
                44100, 8000, "A440.wav");
        audioDataObject2 = new AudioDataObject0x00050002(
                CompressionHandlers.lz4Compress(Files.readAllBytes(Paths.get(
                        IOMethods.getAbsoluteFilePath("testing-files/audio/Choice.wav")
                ))),
                44100, 5000, "Choice.wav");

        projectInfoDataObject1 = new ProjectInfoDataObject0x00050002(
                1, 0, 123.45, 0.01, 0.55, 9000
        );
        projectInfoDataObject2 = new ProjectInfoDataObject0x00050002(
                8, 7, 67.89, -1.23, 0.124, 2048
        );

        musicNotesDataObject1 = new MusicNotesDataObject0x00050002(
                timesToPlaceRectangles1, noteDurations1, noteNums1
        );
        musicNotesDataObject2 = new MusicNotesDataObject0x00050002(
                timesToPlaceRectangles2, noteDurations2, noteNums2
        );

        unchangingDataPropertiesObject1 = new UnchangingDataPropertiesObject0x00050002(
                32 +  // Header section
                        UnchangingDataPropertiesObject.NUM_BYTES_NEEDED +
                        qTransformDataObject1.numBytesNeeded() +
                        audioDataObject1.numBytesNeeded()
        );
        unchangingDataPropertiesObject2 = new UnchangingDataPropertiesObject0x00050002(
                32 +  // Header section
                        UnchangingDataPropertiesObject.NUM_BYTES_NEEDED +
                        qTransformDataObject2.numBytesNeeded() +
                        audioDataObject2.numBytesNeeded()
        );
    }

    @Test
    void testEquals() {
        // Define temporary data objects for testing the initial checks
        ProjectData temp = new ProjectData(
                unchangingDataPropertiesObject1, qTransformDataObject1, audioDataObject1, projectInfoDataObject1,
                musicNotesDataObject1
        );

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data objects from
        UnchangingDataPropertiesObject[] unchangingDataPropertiesObjects = {
                unchangingDataPropertiesObject1, unchangingDataPropertiesObject2
        };
        QTransformDataObject[] qTransformDataObjects = {
                qTransformDataObject1, qTransformDataObject2
        };
        AudioDataObject[] audioDataObjects = {
                audioDataObject1, audioDataObject2
        };
        ProjectInfoDataObject[] projectInfoDataObjects = {
                projectInfoDataObject1, projectInfoDataObject2
        };
        MusicNotesDataObject[] musicNotesDataObjects = {
                musicNotesDataObject1, musicNotesDataObject2
        };

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 5);  // 5 data objects
        for (int[] indices1 : indexProduct) {
            ProjectData one = new ProjectData(
                    unchangingDataPropertiesObjects[indices1[0]],
                    qTransformDataObjects[indices1[1]],
                    audioDataObjects[indices1[2]],
                    projectInfoDataObjects[indices1[3]],
                    musicNotesDataObjects[indices1[4]]
            );

            for (int[] indices2 : indexProduct) {
                ProjectData two = new ProjectData(
                        unchangingDataPropertiesObjects[indices2[0]],
                        qTransformDataObjects[indices2[1]],
                        audioDataObjects[indices2[2]],
                        projectInfoDataObjects[indices2[3]],
                        musicNotesDataObjects[indices2[4]]
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
        ProjectData one = new ProjectData(
                unchangingDataPropertiesObject1, qTransformDataObject1, audioDataObject1, projectInfoDataObject1,
                musicNotesDataObject1
        );
        ProjectData two = new ProjectData(
                unchangingDataPropertiesObject2, qTransformDataObject2, audioDataObject2, projectInfoDataObject2,
                musicNotesDataObject2
        );

        assertEquals(272185181, one.hashCode());
        assertEquals(-1573040154, two.hashCode());
    }
}
