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

import org.javatuples.Triplet;
import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.exceptions.audio.AudioTooLongException;
import site.overwrite.auditranscribe.io.IOMethods;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MusicKeyEstimatorTest {
    @Test
    void getKeyCorrelations() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
        // Test 1
        Audio audio1 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-files/audio/Choice.wav")),
                "Choice.wav",
                AudioProcessingMode.SAMPLES_ONLY
        );

        double[] samples1 = audio1.getMonoSamples();
        double sampleRate1 = audio1.getSampleRate();
        List<Triplet<Integer, Boolean, Double>> corr1 = MusicKeyEstimator.getKeyCorrelations(samples1, sampleRate1, null);

        Triplet<Integer, Boolean, Double> best11 = corr1.get(0);
        Triplet<Integer, Boolean, Double> best12 = corr1.get(1);
        Triplet<Integer, Boolean, Double> best13 = corr1.get(2);

        assertEquals(7, best11.getValue0());
        assertEquals(false, best11.getValue1());
        assertEquals(2, best12.getValue0());
        assertEquals(true, best12.getValue1());
        assertEquals(7, best13.getValue0());
        assertEquals(true, best13.getValue1());

        // Test 2
        Audio audio2 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-files/audio/Trumpet.wav")),
                "Trumpet.wav",
                AudioProcessingMode.SAMPLES_ONLY
        );

        double[] samples2 = audio2.getMonoSamples();
        double sampleRate2 = audio2.getSampleRate();
        List<Triplet<Integer, Boolean, Double>> corr2 = MusicKeyEstimator.getKeyCorrelations(samples2, sampleRate2, null);

        Triplet<Integer, Boolean, Double> best21 = corr2.get(0);
        Triplet<Integer, Boolean, Double> best22 = corr2.get(1);
        Triplet<Integer, Boolean, Double> best23 = corr2.get(2);

        assertEquals(5, best21.getValue0());
        assertEquals(false, best21.getValue1());
        assertEquals(5, best22.getValue0());
        assertEquals(true, best22.getValue1());
        assertEquals(10, best23.getValue0());
        assertEquals(false, best23.getValue1());
    }
}