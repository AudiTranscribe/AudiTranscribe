/*
 * SettingsData.java
 *
 * Created on 2022-05-22
 * Updated on 2022-08-13
 *
 * Description: Class that contains the settings data.
 */

package site.overwrite.auditranscribe.io.json_files.data_encapsulators;

import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.misc.Theme;
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

    // Spectrogram data
    public int colourScaleEnumOrdinal = ColourScale.VIRIDIS.ordinal();
    public int windowFunctionEnumOrdinal = WindowFunction.HANN_WINDOW.ordinal();

    // Miscellaneous data
    public int themeEnumOrdinal = Theme.LIGHT_MODE.ordinal();
}
