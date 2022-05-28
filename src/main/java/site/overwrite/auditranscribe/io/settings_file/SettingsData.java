/*
 * SettingsData.java
 *
 * Created on 2022-05-22
 * Updated on 2022-05-28
 *
 * Description: Class that contains the settings data.
 */

package site.overwrite.auditranscribe.io.settings_file;

import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.spectrogram.ColourScale;

/**
 * Class that contains the settings data.
 */
public class SettingsData {
    // Attributes
    // Appearance data
//    public int themeEnumOrdinal;  // Ordinal of the `Theme` enum value Todo: implement

    // Spectrogram data
    public int colourScaleEnumOrdinal  = ColourScale.VIRIDIS.ordinal();  // Ordinal of the `ColourScale` enum value
    public int windowFunctionEnumOrdinal = WindowFunction.HANN_WINDOW.ordinal();  // Ordinal of the `WindowFunction` enum value

    // Setter methods
    public void setColourScaleEnumOrdinal(ColourScale colourScale) {
        this.colourScaleEnumOrdinal = colourScale.ordinal();
    }

    public void setWindowFunctionEnumOrdinal(WindowFunction windowFunction) {
        this.windowFunctionEnumOrdinal = windowFunction.ordinal();
    }

    //    public void setThemeEnumOrdinal(int themeEnumOrdinal) {  // Todo: implement
//        this.themeEnumOrdinal = themeEnumOrdinal;
//    }
}
