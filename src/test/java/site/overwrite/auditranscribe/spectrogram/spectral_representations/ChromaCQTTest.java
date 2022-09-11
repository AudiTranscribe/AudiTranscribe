/*
 * ChromaCQTTest.java
 * Description: Test `ChromaCQT.java`.
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

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.exceptions.audio.AudioTooLongException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ChromaCQTTest {
    @Test
    void chromaCQT() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
        Audio audio = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-files/audio/Trumpet.wav")),
                "Trumpet.wav",
                AudioProcessingMode.SAMPLES_ONLY
        );

        double[] samples = audio.getMonoSamples();
        double sampleRate = audio.getSampleRate();

        double[][] chroma = ChromaCQT.chromaCQT(samples, sampleRate, 512, UnitConversionUtils.noteToFreq("C1"), 12, 7, 36, null);

        assertEquals(0.21, chroma[0][0], 1e-2);
        assertEquals(0.05, chroma[1][1], 1e-2);
        assertEquals(0.30, chroma[11][23], 1e-2);
        assertEquals(0.61, chroma[5][214], 1e-2);
        assertEquals(0.13, chroma[9][128], 1e-2);
    }
}