/*
 * SettingsData.java
 *
 * Created on 2022-05-22
 * Updated on 2022-05-30
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
    // Attributes
    // Appearance data
    public int themeEnumOrdinal = Theme.LIGHT_MODE.ordinal();  // Ordinal of the `Theme` enum value

    // Spectrogram data
    public int colourScaleEnumOrdinal  = ColourScale.VIRIDIS.ordinal();  // Ordinal of the `ColourScale` enum value
    public int windowFunctionEnumOrdinal = WindowFunction.HANN_WINDOW.ordinal();  // Ordinal of the `WindowFunction` enum value
}
