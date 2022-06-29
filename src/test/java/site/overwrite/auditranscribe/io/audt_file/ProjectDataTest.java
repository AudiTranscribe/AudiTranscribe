/*
 * ProjectDataTest.java
 *
 * Created on 2022-06-29
 * Updated on 2022-06-29
 *
 * Description: Test `ProjectData.java`.
 */

package site.overwrite.auditranscribe.io.audt_file;

import org.javatuples.Triplet;
import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.LZ4;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.*;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.TypeConversionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

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

    GUIDataObject guiDataObject1;
    GUIDataObject guiDataObject2;

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
        Triplet<Byte[], Double, Double> conversionTuple1 =
                QTransformDataObject.qTransformMagnitudesToByteData(qTransformMagnitudes1, null);
        byte[] qTransformBytes1 = TypeConversionUtils.toByteArray(conversionTuple1.getValue0());
        double minMagnitude1 = conversionTuple1.getValue1();
        double maxMagnitude1 = conversionTuple1.getValue2();
        Triplet<Byte[], Double, Double> conversionTuple2 =
                QTransformDataObject.qTransformMagnitudesToByteData(qTransformMagnitudes2, null);
        byte[] qTransformBytes2 = TypeConversionUtils.toByteArray(conversionTuple2.getValue0());
        double minMagnitude2 = conversionTuple2.getValue1();
        double maxMagnitude2 = conversionTuple2.getValue2();

        // Define data to be used within the tests
        qTransformDataObject1 = new QTransformDataObject(
                qTransformBytes1, minMagnitude1, maxMagnitude1
        );
        qTransformDataObject2 = new QTransformDataObject(
                qTransformBytes2, minMagnitude2, maxMagnitude2
        );

        audioDataObject1 = new AudioDataObject(
                LZ4.lz4Compress(Files.readAllBytes(Paths.get(
                        IOMethods.getAbsoluteFilePath("testing-files/audio/A440.mp3")
                ))),
                44100, 8000, "A440.wav");
        audioDataObject2 = new AudioDataObject(
                LZ4.lz4Compress(Files.readAllBytes(Paths.get(
                        IOMethods.getAbsoluteFilePath("testing-files/audio/Choice.wav")
                ))),
                44100, 5000, "Choice.wav");

        guiDataObject1 = new GUIDataObject(
                11, 9, 123.45, 0.01, 0.55, 9000
        );
        guiDataObject2 = new GUIDataObject(
                15, 14, 67.89, -1.23, 0.124, 2048
        );

        musicNotesDataObject1 = new MusicNotesDataObject(
                timesToPlaceRectangles1, noteDurations1, noteNums1
        );
        musicNotesDataObject2 = new MusicNotesDataObject(
                timesToPlaceRectangles2, noteDurations2, noteNums2
        );

        unchangingDataPropertiesObject1 = new UnchangingDataPropertiesObject(
                32 +  // Header section
                        UnchangingDataPropertiesObject.NUM_BYTES_NEEDED +
                        qTransformDataObject1.numBytesNeeded() +
                        audioDataObject1.numBytesNeeded()
        );
        unchangingDataPropertiesObject2 = new UnchangingDataPropertiesObject(
                32 +  // Header section
                        UnchangingDataPropertiesObject.NUM_BYTES_NEEDED +
                        qTransformDataObject2.numBytesNeeded() +
                        audioDataObject2.numBytesNeeded()
        );
    }

    @Test
    void testEquality() {
        // Define temporary data objects for testing the initial checks
        ProjectData tempProjectData = new ProjectData(
                unchangingDataPropertiesObject1, qTransformDataObject1, audioDataObject1, guiDataObject1,
                musicNotesDataObject1
        );

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(tempProjectData, tempProjectData);
        assertNotEquals(tempProjectData, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(tempProjectData, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data objects from
        UnchangingDataPropertiesObject[] unchangingDataPropertiesObjects = new UnchangingDataPropertiesObject[]{
                unchangingDataPropertiesObject1, unchangingDataPropertiesObject2
        };
        QTransformDataObject[] qTransformDataObjects = new QTransformDataObject[]{
                qTransformDataObject1, qTransformDataObject2
        };
        AudioDataObject[] audioDataObjects = new AudioDataObject[]{
                audioDataObject1, audioDataObject2
        };
        GUIDataObject[] guiDataObjects = new GUIDataObject[]{
                guiDataObject1, guiDataObject2
        };
        MusicNotesDataObject[] musicNotesDataObjects = new MusicNotesDataObject[]{
                musicNotesDataObject1, musicNotesDataObject2
        };

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 5);  // 5 data objects
        for (int[] indices1 : indexProduct) {
            ProjectData projectData1 = new ProjectData(
                    unchangingDataPropertiesObjects[indices1[0]],
                    qTransformDataObjects[indices1[1]],
                    audioDataObjects[indices1[2]],
                    guiDataObjects[indices1[3]],
                    musicNotesDataObjects[indices1[4]]
            );

            for (int[] indices2 : indexProduct) {
                ProjectData projectData2 = new ProjectData(
                        unchangingDataPropertiesObjects[indices2[0]],
                        qTransformDataObjects[indices2[1]],
                        audioDataObjects[indices2[2]],
                        guiDataObjects[indices2[3]],
                        musicNotesDataObjects[indices2[4]]
                );

                // Check equality
                if (indices1 == indices2) {
                    assertEquals(projectData1, projectData2);
                    assertEquals(projectData2, projectData1);
                } else {
                    assertNotEquals(projectData1, projectData2);
                    assertNotEquals(projectData2, projectData1);
                }
            }
        }
    }

    @Test
    void testHashCode() {
        // Define project data objects for testing
        ProjectData projectData1 = new ProjectData(
                unchangingDataPropertiesObject1, qTransformDataObject1, audioDataObject1, guiDataObject1,
                musicNotesDataObject1
        );
        ProjectData projectData2 = new ProjectData(
                unchangingDataPropertiesObject2, qTransformDataObject2, audioDataObject2, guiDataObject2,
                musicNotesDataObject2
        );

        assertEquals(814949758, projectData1.hashCode());
        assertEquals(544922374, projectData2.hashCode());
    }
}
