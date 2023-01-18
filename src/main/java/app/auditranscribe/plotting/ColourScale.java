/*
 * ColourScale.java
 * Description: Enum that contains the possible colour scales for the spectrogram.
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

package app.auditranscribe.plotting;

import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

/**
 * Enum that contains the possible colour scales for the spectrogram.<br>
 * Colour scale values are taken from Plotly and its
 * <a href="https://plotly.com/python/builtin-colorscales/">Built-In Colour Scales</a>.
 */
@ExcludeFromGeneratedCoverageReport
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
    MAGMA(new int[]{
            0xfcfdbf, 0xfeca8d, 0xfd9668, 0xf1605d, 0xcd4071, 0x9e2f7f, 0x721f81, 0x440f76, 0x180f3d, 0x000004
    }, "Magma"),
    PLASMA(new int[]{
            0xf0f921, 0xfdca26, 0xfb9f3a, 0xed7953, 0xd8576b, 0xbd3786, 0x9c179e, 0x7201a8, 0x46039f, 0x0d0887
    }, "Plasma"),
    TURBO(new int[]{
            0x7a0402, 0xb11901, 0xd93806, 0xf36315, 0xfe9b2d, 0xf3c63a, 0xd1e834, 0xa4fc3b, 0x61fc6c, 0x24eca6,
            0x1bcfd4, 0x39a2fc, 0x4675ed, 0x4145ab, 0x30123b
    }, "Turbo"),
    BLUES(new int[]{
            0xf7fbff, 0xdeebf7, 0xc6dbef, 0x9ecae1, 0x6baed6, 0x4292c6, 0x2171b5, 0x08519c, 0x08306b
    }, "Blues"),
    ORANGES(new int[]{
            0xfff5eb, 0xfee6ce, 0xfdd0a2, 0xfdae6b, 0xfd8d3c, 0xf16913, 0xd94801, 0xa63603, 0x7f2704
    }, "Oranges"),
    THERMAL(new int[]{
            0xe7fa5a, 0xf6d346, 0xfbad3c, 0xf68b45, 0xe17161, 0xc16479, 0x9e5987, 0x7e4d8f, 0x5d3e99, 0x35329b,
            0x0d3064, 0x032333
    }, "Thermal"),
    HALINE(new int[]{
            0xfdee99, 0xd4e170, 0xa0d65b, 0x6fc66b, 0x51b27c, 0x419d85, 0x358888, 0x267489, 0x125f8e, 0x0f4799,
            0x2a23a0, 0x29186b
    }, "Haline"),
    SOLAR(new int[]{
            0xe0fd4a, 0xdede3b, 0xd9c02c, 0xd1a420, 0xc78916, 0xbc6f13, 0xae5814, 0x9d4219, 0x872f20, 0x6c2424,
            0x4f1c21, 0x331317
    }, "Solar"),
    ICE(new int[]{
            0xeafcfd, 0xc0e5e8, 0x95cfd8, 0x72b8cd, 0x599fc4, 0x4886bb, 0x3e6db2, 0x3e53a0, 0x3a3c7d, 0x2c2a57,
            0x191933, 0x030512
    }, "Ice");

    // Attributes
    public final int[] colours;
    private final String name;

    // Enum constructor
    ColourScale(int[] colours, String name) {
        this.colours = colours;
        this.name = name;
    }

    // Public methods
    @Override
    public String toString() {
        return name;
    }
}
