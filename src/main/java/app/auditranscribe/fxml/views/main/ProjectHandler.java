/*
 * ProjectHandler.java
 * Description: Handles the interaction of AudiTranscribe projects.
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

package app.auditranscribe.fxml.views.main;

import app.auditranscribe.io.audt_file.AUDTFileConstants;
import app.auditranscribe.io.audt_file.ProjectData;
import app.auditranscribe.io.audt_file.base.AUDTFileWriter;
import app.auditranscribe.io.audt_file.base.data_encapsulators.MusicNotesDataObject;
import app.auditranscribe.io.audt_file.base.data_encapsulators.ProjectInfoDataObject;
import app.auditranscribe.io.exceptions.InvalidFileVersionException;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

import java.io.IOException;

/**
 * Handles the interaction of AudiTranscribe projects.
 */
@ExcludeFromGeneratedCoverageReport
public final class ProjectHandler {
    private ProjectHandler() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that handles the saving of an AudiTranscribe project.
     *
     * @param filepath          <b>Absolute</b> path to the AUDT file.
     * @param numSkippableBytes Number of bytes to skip before writing the data.
     * @param guiData           <code>ProjectInfoDataObject</code> object that contains the GUI data.
     * @param musicNotesData    <code>MusicNotesDataObject</code> object that contains the music data.
     * @throws IOException If the writing to file encounters an error.
     */
    public static void saveProject(
            String filepath, int numSkippableBytes, ProjectInfoDataObject guiData, MusicNotesDataObject musicNotesData
    ) throws IOException {
        try {
            // Declare the file writer object
            AUDTFileWriter fileWriter = AUDTFileWriter.getWriter(
                    AUDTFileConstants.FILE_VERSION_NUMBER, filepath, numSkippableBytes
            );

            // Write data to the file
            fileWriter.writeProjectInfoData(guiData);
            fileWriter.writeMusicNotesData(musicNotesData);

            fileWriter.writeToFile();
        } catch (InvalidFileVersionException ignored) {  // Impossible for the version to be wrong
        }
    }

    /**
     * Method that handles the saving of an AudiTranscribe project.
     *
     * @param filepath    <b>Absolute</b> path to the AUDT file.
     * @param projectData Data object that stores all the data for the project.
     * @throws IOException If the writing to file encounters an error.
     */
    public static void saveProject(String filepath, ProjectData projectData) throws IOException {
        try {
            // Declare the file writer object
            AUDTFileWriter fileWriter = AUDTFileWriter.getWriter(AUDTFileConstants.FILE_VERSION_NUMBER, filepath);

            // Write data to the file
            fileWriter.writeUnchangingDataProperties(projectData.unchangingDataProperties);
            fileWriter.writeQTransformData(projectData.qTransformData);
            fileWriter.writeAudioData(projectData.audioData);
            fileWriter.writeProjectInfoData(projectData.projectInfoData);
            fileWriter.writeMusicNotesData(projectData.musicNotesData);

            fileWriter.writeToFile();
        } catch (InvalidFileVersionException ignored) {  // Impossible for the version to be wrong
        }
    }
}
