/*
 * SpectralHelpersTest.java
 * Description: Test `SpectralHelpers.java`.
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

package site.overwrite.auditranscribe.spectrogram;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.exceptions.AudioTooLongException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.utils.MathUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SpectralHelpersTest {
    // Load samples and sample rate for some tests
    Audio audio = new Audio(
            new File(IOMethods.getAbsoluteFilePath("testing-files/audio/Trumpet.wav")),
            AudioProcessingMode.SAMPLES
    );

    double[] samples = audio.getMonoSamples();
    double sampleRate = audio.getSampleRate();

    // Initialization method
    SpectralHelpersTest() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
    }

    // Tests
    @Test
    void computeAlpha() {
        assertEquals(0.6, SpectralHelpers.computeAlpha(1));
        assertEquals(MathUtils.round((double) 1 / 3, 6), MathUtils.round(SpectralHelpers.computeAlpha(2), 6));
    }

    @Test
    void estimateTuning() {
        assertEquals(-0.09, SpectralHelpers.estimateTuning(samples, sampleRate), 1e-5);
    }

    @Test
    void estimateTuningWithWeirdThreshold() {
        assertEquals(-0.5, SpectralHelpers.estimateTuning(samples, sampleRate, 1e6), 1e-5);
    }
}