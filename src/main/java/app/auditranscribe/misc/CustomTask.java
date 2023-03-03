/*
 * CustomTask.java
 * Description: A custom task class extending the access of some methods of the JavaFX `Task` class.
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

import javafx.concurrent.Task;

/**
 * A custom task class extending the access of some methods of the JavaFX <code>Task</code> class.
 *
 * @param <V> Type that is returned by the task.
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
     * Uses the message of the task as the name of the task.
     */
    public CustomTask() {
        this.name = this.getMessage();
    }

    // Public methods
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
}
