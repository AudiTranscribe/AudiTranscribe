/*
 * SettingsData.java
 * Description: Class that contains the settings data.
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

package site.overwrite.auditranscribe.io.data_files.data_encapsulators;

import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.music.notes.NoteQuantizationUnit;
import site.overwrite.auditranscribe.spectrogram.ColourScale;

/**
 * Class that contains the settings data.
 */
public class SettingsData {
    // Audio data
    public double notePlayingDelayOffset = 0.2;  // In seconds; to account for note playing delay
    public String ffmpegInstallationPath = null;  // Path to the ffmpeg installation

    // I/O data
    public int autosaveInterval = 5;  // In minutes
    public int logFilePersistence = 5;  // In days

    // Transcription data
    public int colourScaleEnumOrdinal = ColourScale.VIRIDIS.ordinal();
    public int windowFunctionEnumOrdinal = WindowFunction.HANN_WINDOW.ordinal();
    public int noteQuantizationUnitEnumOrdinal = NoteQuantizationUnit.THIRTY_SECOND_NOTE.ordinal();

    // Miscellaneous data
    public int themeEnumOrdinal = Theme.LIGHT_MODE.ordinal();
    public int checkForUpdateInterval = 24;  // In hours
}
