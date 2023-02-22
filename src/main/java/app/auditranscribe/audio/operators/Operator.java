/*
 * Operator.java
 * Description: An abstract thread that operates on streams of bytes.
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

import app.auditranscribe.audio.Audio;
import app.auditranscribe.generic.LoggableClass;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An abstract thread that operates on streams of bytes.
 */
public abstract class Operator extends LoggableClass implements Runnable {
    // Attributes
    protected BlockingQueue<Double> inputBuffer = new LinkedBlockingQueue<>(8192);

    private volatile boolean isRunning = true;
    private volatile Audio caller = null;
    private volatile int channelNum;

    // Public methods

    /**
     * Method that processes the data.
     *
     * @return Processed data.
     */
    public abstract double[] process() throws InterruptedException;

    /**
     * Inputs data to be processed.<br>
     * If the operator's buffer is full, this will block the thread until space is available.
     *
     * @param audio      Audio object to return the data to.
     * @param channelNum Integer identifying the audio channel that the data came from.
     * @param data       Data to be processed.
     * @throws InterruptedException If the operator was interrupted.
     */
    public void call(Audio audio, int channelNum, double[] data) throws InterruptedException {
        this.caller = audio;
        this.channelNum = channelNum;
        for (double f : data) {
            inputBuffer.put(f);
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            if (caller != null) {
                double[] output;
                try {
                    output = process();
                    caller.answer(channelNum, output);
                } catch (InterruptedException e) {
                    logException(e);
                }
            }
        }
    }

    /**
     * Stops the thread and releases any of its resources.
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * Finds how many more elements could be added without blocking.
     *
     * @return Capacity remaining before {@link #call(Audio, int, double[])} will block.
     */
    public int remainingCapacity() {
        return inputBuffer.remainingCapacity();
    }
}
