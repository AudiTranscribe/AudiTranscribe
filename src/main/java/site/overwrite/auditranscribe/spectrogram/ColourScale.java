/*
 * ColourScale.java
 *
 * Created on 2022-02-20
 * Updated on 2022-02-20
 *
 * Description: Enum that contains some colour scales for the spectrogram.
 */

package site.overwrite.auditranscribe.spectrogram;

public enum ColourScale {
    // Enum values
    BLUES(new int[] {0xf7fbff, 0xdeebf7, 0xc6dbef, 0x9ecae1, 0x6baed6, 0x4292c6, 0x2171b5, 0x08519c, 0x08306b}),
    ORANGES(new int[] {0xfff5eb, 0xfee6ce, 0xfdd0a2, 0xfdae6b, 0xfd8d3c, 0xf16913, 0xd94801, 0xa63603, 0x7f2704}),
    VIRIDIS(new int[] {0xfde725, 0xb5de2b, 0x6ece58, 0x35b779, 0x1f9e89, 0x26828e, 0x31688e, 0x3e4989, 0x482878, 0x440154});

    // Attributes
    public final int[] colours;

    // Enum constructor
    ColourScale(int[] colours) {
        this.colours = colours;
    }
}
