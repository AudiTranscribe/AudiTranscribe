/*
 * SettingsData.java
 *
 * Created on 2022-05-22
 * Updated on 2022-05-22
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
    public int colourScaleEnumOrdinal;  // Ordinal of the `ColourScale` enum value
    public int windowFunctionEnumOrdinal;  // Ordinal of the `WindowFunction` enum value

    /**
     * Initialization method for a new <code>SettingsData</code> object.<br>
     * Note that this should only be called if a new, default <code>SettingsData</code> object is
     * needed. <b>This constructor accepts no parameters</b>, so only the default values will be
     * used.
     */
    public SettingsData() {
        colourScaleEnumOrdinal = ColourScale.VIRIDIS.ordinal();
        windowFunctionEnumOrdinal = WindowFunction.HANN_WINDOW.ordinal();
    }

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
