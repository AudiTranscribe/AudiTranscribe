/*
 * CustomTask.java
 * Description: A custom task class where some protected methods are made public.
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

package site.overwrite.auditranscribe.misc;

import javafx.concurrent.Task;

/**
 * A custom task class where some protected methods are made public.
 *
 * @param <V> Type of value that is returned by the custom task.
 */
@ExcludeFromGeneratedCoverageReport
public abstract class CustomTask<V> extends Task<V> {
    // Attributes
    public String name;

    /**
     * Initializes a new instance of the <code>CustomTask</code> class.
     *
     * @param name Name of the task.
     */
    public CustomTask(String name) {
        this.name = name;
    }

    /**
     * Initializes a new instance of the <code>CustomTask</code> class.<br>
     * This assumes that the name of the task is the message of the task.
     */
    public CustomTask() {
        this.name = this.getMessage();
    }

    // Public methods

    /**
     * Alias to {@link #updateMessage(String)}.
     *
     * @param s The message to set.
     */
    public void setMessage(String s) {
        updateMessage(s);
    }

    // Overridden methods
    @Override
    public void updateProgress(long current, long total) {
        super.updateProgress(current, total);
    }

    @Override
    public void updateProgress(double current, double total) {
        super.updateProgress(current, total);
    }

    @Override
    public void updateMessage(String s) {
        super.updateMessage(s);
    }

    @Override
    abstract protected V call() throws Exception;
}
