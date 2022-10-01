/*
 * MusicKeyEstimatorTest.java
 * Description: Tests `MusicKeyEstimator.java`.
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

package site.overwrite.auditranscribe.music;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.exceptions.audio.AudioTooLongException;
import site.overwrite.auditranscribe.exceptions.generic.ValueException;
import site.overwrite.auditranscribe.io.IOMethods;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MusicKeyEstimatorTest {
    @Test
    void getMostLikelyKeys() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
        // Test 1
        Audio audio1 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-files/audio/Choice.wav")),
                AudioProcessingMode.SAMPLES_ONLY
        );

        double[] samples1 = audio1.getMonoSamples();
        double sampleRate1 = audio1.getSampleRate();

        MusicKeyEstimator estimator1 = new MusicKeyEstimator(samples1, sampleRate1);
        List<MusicKey> mostLikelyKeys1 = estimator1.getMostLikelyKeys(3, null);

        assertEquals(List.of(MusicKey.G_MAJOR, MusicKey.D_MINOR, MusicKey.G_MINOR), mostLikelyKeys1);
//        assertEquals(List.of(MusicKey.G_MAJOR, MusicKey.D_MINOR, MusicKey.F_SHARP_MAJOR), mostLikelyKeys1);  // Todo: disable after tuning fix

        // Test 2
        Audio audio2 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-files/audio/Trumpet.wav")),
                AudioProcessingMode.SAMPLES_ONLY
        );

        double[] samples2 = audio2.getMonoSamples();
        double sampleRate2 = audio2.getSampleRate();

        MusicKeyEstimator estimator2 = new MusicKeyEstimator(samples2, sampleRate2);
        List<MusicKey> mostLikelyKeys2 = estimator2.getMostLikelyKeys(4, null);

        assertEquals(
                List.of(MusicKey.F_MAJOR, MusicKey.F_MINOR, MusicKey.B_FLAT_MAJOR, MusicKey.A_SHARP_MINOR),
                mostLikelyKeys2
        );
//        assertEquals(
//                List.of(MusicKey.F_MAJOR, MusicKey.F_MINOR, MusicKey.A_SHARP_MINOR, MusicKey.B_FLAT_MINOR),
//                mostLikelyKeys2
//        );  // Todo: disable after tuning fix

        // Test 3: Invalid key values
        assertThrowsExactly(ValueException.class, () -> estimator1.getMostLikelyKeys(0, null));
        assertThrowsExactly(ValueException.class, () -> estimator2.getMostLikelyKeys(-1, null));
        assertThrowsExactly(ValueException.class, () -> estimator1.getMostLikelyKeys(31, null));
        assertThrowsExactly(ValueException.class, () -> estimator2.getMostLikelyKeys(1337, null));
    }
}