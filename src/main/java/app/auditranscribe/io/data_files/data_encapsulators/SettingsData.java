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

package app.auditranscribe.io.data_files.data_encapsulators;

import app.auditranscribe.fxml.Theme;
import app.auditranscribe.fxml.plotting.ColourScale;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.music.NoteUnit;
import app.auditranscribe.signal.windowing.SignalWindow;

/**
 * Class that contains the settings data.
 */
@ExcludeFromGeneratedCoverageReport
public class SettingsData {
    // Constants
    public final static int PLAYBACK_BUFFER_SIZE = 1024;  // In bytes

    public final static int AUTOSAVE_INTERVAL = 5;  // In minutes
    public final static int LOG_FILE_PERSISTENCE = 5;  // In days

    public final static int COLOUR_SCALE_ENUM_ORDINAL = ColourScale.VIRIDIS.ordinal();
    public final static int WINDOW_FUNCTION_ENUM_ORDINAL = SignalWindow.HANN_WINDOW.ordinal();
    public final static int NOTE_QUANTIZATION_UNIT_ENUM_ORDINAL = NoteUnit.THIRTY_SECOND_NOTE.ordinal();

    public final static int THEME_ENUM_ORDINAL = Theme.DARK.ordinal();

    // Audio data
    public String ffmpegInstallationPath = null;  // Path to the ffmpeg installation
    public int playbackBufferSize = PLAYBACK_BUFFER_SIZE;

    // Input/output data
    public int autosaveInterval = AUTOSAVE_INTERVAL;
    public int logFilePersistence = LOG_FILE_PERSISTENCE;

    // Transcription data
    public int colourScaleEnumOrdinal = COLOUR_SCALE_ENUM_ORDINAL;
    public int windowFunctionEnumOrdinal = WINDOW_FUNCTION_ENUM_ORDINAL;
    public int noteQuantizationUnitEnumOrdinal = NOTE_QUANTIZATION_UNIT_ENUM_ORDINAL;

    // Miscellaneous data
    public int themeEnumOrdinal = THEME_ENUM_ORDINAL;
}
