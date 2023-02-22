/*
 * PhaseVocoderOperator.java
 * Description: Phase vocoder operator that operates on audio bytes.
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

package app.auditranscribe.audio.operators;

import app.auditranscribe.misc.CapacityQueue;
import app.auditranscribe.misc.Complex;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.signal.representations.FFT;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.utils.ArrayUtils;
import app.auditranscribe.utils.MathUtils;
import app.auditranscribe.utils.TypeConversionUtils;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Phase vocoder operator that operates on audio bytes.
 */
@ExcludeFromGeneratedCoverageReport
public class PhaseVocoderOperator extends TimeStretchOperator {
    // Attributes
    private final int processingLength;
    private final int analysisLength;
    private final SignalWindow windowFunction;

    private int analysisOutputSize;
    private double samplesStretchFactor;  // Slightly different to `stretchFactor`; this is for discrete stretching

    double[] prevPhases;
    double[] principalAdjustedPhases;
    double[] phaseAdvance;  // Expected phase advance

    private final CapacityQueue<Double> input;
    private final CapacityQueue<Double> output;

    /**
     * Initializes a new phase vocoder operator.
     *
     * @param stretchFactor    Initial time stretch factor.
     * @param processingLength Number of samples that this operator will process per call.
     * @param analysisLength   Number of additional samples to read in once required.
     * @param windowFunction   Signal window function to apply to the samples.
     */
    public PhaseVocoderOperator(
            double stretchFactor, int processingLength, int analysisLength, SignalWindow windowFunction
    ) {
        super(stretchFactor);
        this.processingLength = processingLength;
        this.analysisLength = analysisLength;
        this.windowFunction = windowFunction;

        prevPhases = new double[processingLength];
        principalAdjustedPhases = new double[processingLength];
        phaseAdvance = ArrayUtils.linspace(0, analysisLength * Math.PI, processingLength);

        input = new CapacityQueue<>(processingLength);
        output = new CapacityQueue<>(processingLength);

        // Fill in output buffer
        for (int i = 0; i < output.getMaxSize(); i++) {
            output.offer(0.);
        }
    }

    // Public methods
    @Override
    public double[] process() throws InterruptedException {
        // Check that we have enough to process
        if (inputBuffer.size() < processingLength) {
            return new double[0];
        }

        // Update attributes for this loop
        this.analysisOutputSize = (int) (stretchFactor * analysisLength);
        this.samplesStretchFactor = (double) analysisOutputSize / analysisLength;

        // Get the next set of windowed samples
        double[] rawSamples = getNextSample();
        double[] windowedSamples = windowSamples(rawSamples);

        // Shift samples for the FFT so that FFT application would be 'smoother'
        fftShift(windowedSamples);

        // Time stretch the windowed samples
        double[] stretchedSamples = timeStretchSamples(windowedSamples);

        // Overlap-add and slide the samples
        LinkedList<Double> finishedSamples = overlapAddAndSlide(stretchedSamples);

        // Correct scaling due to overlap-add
        double[] finalOutput = new double[finishedSamples.size()];
        double overlapScaling = (double) processingLength / (analysisOutputSize * 2.);
        for (int i = 0; i < finalOutput.length; i++) {
            // Note: no `NullPointerException` will occur here because the number of element we are polling is the same
            //       as the length of the linked list
            //noinspection DataFlowIssue
            finalOutput[i] = finishedSamples.poll() / overlapScaling;
        }
        return finalOutput;
    }

    // Private methods

    /**
     * Obtains the next sample window from the <code>input</code> buffer.
     *
     * @return An array of doubles, representing the sample window.
     * @throws InterruptedException If the process was interrupted whilst reading from the input buffer.
     */
    private double[] getNextSample() throws InterruptedException {
        if (input.size() == 0) {
            while (input.size() < input.getMaxSize()) {
                input.offer(inputBuffer.take());
            }
        } else {
            for (int i = 0; i < analysisLength; i++) {
                input.offer(inputBuffer.take());
            }
        }
        return TypeConversionUtils.toDoubleArray(input.toArray(new Double[0]));
    }

    /**
     * Windows the samples using the provided window function.
     *
     * @param rawSamples The raw samples to window.
     * @return Windowed samples.
     */
    private double[] windowSamples(double[] rawSamples) {
        // Generate the window
        double[] window = windowFunction.window.generateWindow(processingLength, false);

        // Window the samples
        double[] windowedSamples = new double[rawSamples.length];
        for (int i = 0; i < rawSamples.length; i++) {
            windowedSamples[i] = rawSamples[i] * window[i];
        }
        return windowedSamples;
    }

    /**
     * Shift samples appropriately for 'smoother' application of FFT.
     *
     * @param data Samples to apply the shift to.
     */
    private static void fftShift(double[] data) {
        if (data.length % 2 == 0) {
            // Move second half elements to the first half, and vice versa
            for (int i = 0; i < data.length / 2; i++) {
                double tmp = data[i];
                data[i] = data[i + data.length / 2];
                data[i + data.length / 2] = tmp;
            }
        } else {
            int numRemaining = data.length;  // Number of remaining elements to process
            int curr = 0;                    // Current element index
            double save = data[0];

            while (numRemaining >= 0) {
                double next = data[(curr + data.length / 2) % data.length];
                data[(curr + data.length / 2) % data.length] = save;

                save = next;
                curr = (curr + data.length / 2) % data.length;
                numRemaining--;
            }
        }
    }

    /**
     * Time stretches the input samples.
     *
     * @param samples Samples to time stretch.
     * @return Time-stretched samples.
     */
    private double[] timeStretchSamples(double[] samples) {
        // Apply FFT to samples
        Complex[] complexes = new Complex[samples.length];
        for (int i = 0; i < samples.length; i++) {
            complexes[i] = new Complex(samples[i]);
        }
        Complex[] fftOut = FFT.fft(complexes);

        // Obtain magnitudes and phases
        double[] mags = new double[fftOut.length];
        double[] phases = new double[fftOut.length];

        for (int i = 0; i < fftOut.length; i++) {
            mags[i] = fftOut[i].abs();
            phases[i] = fftOut[i].phase();
        }

        // Compute change in phase
        double[] phaseDelta = new double[processingLength];
        for (int i = 0; i < processingLength; i++) {
            double diff = phases[i] - prevPhases[i] - phaseAdvance[i];
            phaseDelta[i] = phaseAdvance[i] + MathUtils.principalArg(diff);
            principalAdjustedPhases[i] = MathUtils.principalArg(
                    principalAdjustedPhases[i] + phaseDelta[i] * samplesStretchFactor
            );
        }

        // Update phases of the elements
        for (int i = 0; i < fftOut.length; i++) {
            fftOut[i] = Complex.fromMagnitudeAndPhase(mags[i], principalAdjustedPhases[i]);
        }

        // Invert the FFT and retrieve real parts
        complexes = FFT.ifft(fftOut);
        double[] modified = new double[complexes.length];
        for (int i = 0; i < modified.length; i++) {
            modified[i] = complexes[i].re;
        }

        // Undo shift
        fftShift(modified);

        // Apply window on the samples again
        modified = windowSamples(modified);

        // Update previous phases
        prevPhases = Arrays.copyOf(phases, prevPhases.length);

        return modified;
    }

    /**
     * Perform overlap-add and sliding of the samples.
     *
     * @param stretchedSamples Samples to overlap-add and slide.
     * @return Processed samples.
     */
    private LinkedList<Double> overlapAddAndSlide(double[] stretchedSamples) {
        LinkedList<Double> finishedBytes = new LinkedList<>();
        for (int i = 0; i < analysisOutputSize; i++) {
            finishedBytes.add(output.poll());
            output.offer(0.);
        }
        Double[] outBytes = output.toArray(new Double[0]);

        for (int i = 0; i < outBytes.length; i++) {
            outBytes[i] = stretchedSamples[i] + outBytes[i];
        }
        output.addAll(Arrays.asList(outBytes));
        return finishedBytes;
    }
}
