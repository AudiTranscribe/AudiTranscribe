/*
 * StoppableThread.java
 * Description: Class that implements a thread that can be stopped.
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

package app.auditranscribe.misc;

import app.auditranscribe.generic.LoggableClass;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that implements a thread that can be stopped.
 */
public abstract class StoppableThread extends LoggableClass implements Runnable {
    // Attributes
    private Thread worker;  // Thread that actually runs the code we want
    protected AtomicBoolean running = new AtomicBoolean(false);  // Whether the code is running
    protected AtomicBoolean stopped = new AtomicBoolean(true);  // Whether the thread has stopped

    // Getter/Setter methods

    public boolean getIsRunning() {
        return running.get();
    }

    public boolean getIsStopped() {
        return stopped.get();
    }

    // Public methods

    /**
     * Starts the thread.
     */
    public void start() {
        worker = new Thread(this);
        worker.start();
    }

    /**
     * Causes the thread to stop executing.
     */
    public void stop() {
        running.set(false);  // Will interrupt the execution of the code
    }

    /**
     * Interrupts this thread.
     */
    public void interrupt() {
        stop();
        worker.interrupt();
    }

    /**
     * Waits at most <code>millis</code> milliseconds plus <code>nanos</code> nanoseconds for this
     * thread to die.<br>
     * If both arguments are 0, it means to wait forever.
     *
     * @param millis The time to wait in milliseconds.
     * @param nanos  0-999999 additional nanoseconds to wait.
     * @throws InterruptedException If any thread has interrupted the current thread. The
     *                              <em>interrupted status</em> of the current thread is cleared
     *                              when this exception is thrown.
     */
    public void join(long millis, int nanos) throws InterruptedException {
        worker.join(millis, nanos);
    }

    /**
     * Waits at most <code>millis</code> milliseconds for this thread to die.<br>
     * A timeout of 0 means to wait forever.
     *
     * @param millis The time to wait in milliseconds.
     * @throws InterruptedException If any thread has interrupted the current thread. The
     *                              <em>interrupted status</em> of the current thread is cleared
     *                              when this exception is thrown.
     */
    public void join(long millis) throws InterruptedException {
        worker.join(millis);
    }

    /**
     * Waits for this thread to die.<br>
     * An invocation of this method behaves in exactly the same way as the invocation
     * {@link #join(long) join(0)}.
     *
     * @throws InterruptedException If any thread has interrupted the current thread. The
     *                              <em>interrupted status</em> of the current thread is cleared
     *                              when this exception is thrown.
     */
    public void join() throws InterruptedException {
        worker.join();
    }

    /**
     * Main code to execute within the thread.
     * The basic structure of the code should look like:
     * <pre><code>
     * while (running.get()) {
     *     try {
     *         // Do something
     *     } catch (InterruptedException e) {
     *         Thread.currentThread().interrupt();
     *     }
     * }
     * </code></pre>
     */
    public abstract void runner();

    @Override
    public void run() {
        running.set(true);
        stopped.set(false);
        runner();  // Run code
        stopped.set(true);
    }
}
