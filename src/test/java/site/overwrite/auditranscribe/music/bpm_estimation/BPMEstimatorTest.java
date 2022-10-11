/*
 * BPMEstimatorTest.java
 * Description: Test `BPMEstimator.java`.
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

package site.overwrite.auditranscribe.music.bpm_estimation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.exceptions.AudioTooLongException;
import site.overwrite.auditranscribe.misc.exceptions.ValueException;
import site.overwrite.auditranscribe.io.IOMethods;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BPMEstimatorTest {
    // Get the audio files
    Audio audio1 = new Audio(
            new File(IOMethods.getAbsoluteFilePath("testing-files/audio/175bpm.wav")),
            AudioProcessingMode.SAMPLES
    );
    Audio audio2 = new Audio(
            new File(IOMethods.getAbsoluteFilePath("testing-files/audio/137bpmNoisy.wav")),
            AudioProcessingMode.SAMPLES
    );

    // Extract samples and sample rate
    double[] samples1 = audio1.getMonoSamples();
    double sampleRate1 = audio1.getSampleRate();

    double[] samples2 = audio2.getMonoSamples();
    double sampleRate2 = audio2.getSampleRate();

    // Initialization method
    BPMEstimatorTest() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    void estimateBPM() {
        // Check sample rate and the number of samples
        assertEquals(44100, sampleRate1, "Audio 1 sample rate is not 44100.");
        assertEquals(418950, samples1.length, "Audio 1 does not have correct sample length.");

        assertEquals(44100, sampleRate2, "Audio 2 sample rate is not 44100.");
        assertEquals(441000, samples2.length, "Audio 2 does not have correct sample length.");

        // Generate the estimated BPMs
        List<Double> bpms1 = BPMEstimator.estimate(samples1, sampleRate1);
        List<Double> bpms2 = BPMEstimator.estimate(samples2, sampleRate2);

        // Check the number of elements in the lists
        assertEquals(1, bpms1.size());
        assertEquals(1, bpms2.size());

        // Check the values of the elements
        assertEquals(87.59269068, bpms1.get(0), 1e-5);  // Surprisingly we did not estimate 175 bpm
        assertEquals(135.99917763, bpms2.get(0), 1e-5);
    }

    @Test
    void checkExceptionThrown() {
        // Will throw the " The `winLength` must be a positive integer" exception
        assertThrowsExactly(ValueException.class, () -> BPMEstimator.estimate(samples1, 1));
    }
}