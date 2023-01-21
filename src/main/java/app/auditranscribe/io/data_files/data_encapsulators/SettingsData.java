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
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.plotting.ColourScale;
import app.auditranscribe.signal.windowing.SignalWindow;

/**
 * Class that contains the settings data.
 */
@ExcludeFromGeneratedCoverageReport
public class SettingsData {
    // Audio data
    public String ffmpegInstallationPath = null;  // Path to the ffmpeg installation

    // Input/output data
    public int autosaveInterval = 5;  // In minutes
    public int logFilePersistence = 5;  // In days

    // Transcription data
    public int colourScaleEnumOrdinal = ColourScale.VIRIDIS.ordinal();
    public int windowFunctionEnumOrdinal = SignalWindow.HANN_WINDOW.ordinal();

    // Miscellaneous data
    public int themeEnumOrdinal = Theme.DARK.ordinal();
}
