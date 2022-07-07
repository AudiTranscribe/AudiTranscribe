/*
 * ColourScale.java
 *
 * Created on 2022-02-20
 * Updated on 2022-07-07
 *
 * Description: Enum that contains the possible colour scales for the spectrogram.
 */

package site.overwrite.auditranscribe.spectrogram;

/**
 * Enum that contains the possible colour scales for the spectrogram.<br>
 * Colour scale values are taken from Plotly and its
 * <a href="https://plotly.com/python/builtin-colorscales/">Built-In Colour Scales</a>.
 */
public enum ColourScale {
    // Enum values
    VIRIDIS(new int[]{
            0xfde725, 0xb5de2b, 0x6ece58, 0x35b779, 0x1f9e89, 0x26828e, 0x31688e, 0x3e4989, 0x482878, 0x440154
    }, "Viridis"),
    CIVIDIS(new int[]{
            0xfee838, 0xe1cc55, 0xc3b369, 0xa59c74, 0x8a8678, 0x707173, 0x575d6d, 0x3b496c, 0x123570, 0x00224e
    }, "Cividis"),
    INFERNO(new int[]{
            0xfcffa4, 0xf7d13d, 0xfb9b06, 0xed6925, 0xcf4446, 0xa52c60, 0x781c6d, 0x4a0c6b, 0x1b0c41, 0x000004
    }, "Inferno"),
    BLUES(new int[]{
            0xf7fbff, 0xdeebf7, 0xc6dbef, 0x9ecae1, 0x6baed6, 0x4292c6, 0x2171b5, 0x08519c, 0x08306b
    }, "Blues"),
    ORANGES(new int[]{
            0xfff5eb, 0xfee6ce, 0xfdd0a2, 0xfdae6b, 0xfd8d3c, 0xf16913, 0xd94801, 0xa63603, 0x7f2704
    }, "Oranges"),
    PLASMA(new int[]{
            0xf0f921, 0xfdca26, 0xfb9f3a, 0xed7953, 0xd8576b, 0xbd3786, 0x9c179e, 0x7201a8, 0x46039f, 0x0d0887
    }, "Plasma");

    // Attributes
    public final int[] colours;
    private final String name;

    // Enum constructor
    ColourScale(int[] colours, String name) {
        this.colours = colours;
        this.name = name;
    }

    // Override methods
    @Override
    public String toString() {
        return name;
    }
}
