/*
 * DownloadTask.java
 * Description: A task class that helps expose the download amount to other classes.
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

package app.auditranscribe.network;

import app.auditranscribe.misc.CustomLogger;
import app.auditranscribe.misc.CustomTask;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.logging.Level;

/**
 * A task class that helps expose the download amount to other classes.
 *
 * @param <V> Type that is returned by the download task.
 */
public abstract class DownloadTask<V> extends CustomTask<V> {
    // Attributes
    private int downloadFileSize;
    private final IntegerProperty downloadedAmount = new SimpleIntegerProperty(0);

    /**
     * Initializes a new <code>DownloadTask</code>.
     */
    public DownloadTask() {
        super();

        // Create a change listener for the progress property
        progressProperty().addListener(new ProgressPropertyListener());
    }

    // Getter/Setter methods
    public int getDownloadFileSize() {
        return downloadFileSize;
    }

    public void setDownloadFileSize(int downloadFileSize) {
        this.downloadFileSize = downloadFileSize;
    }

    public IntegerProperty downloadedAmountProperty() {
        return downloadedAmount;
    }

    public int getDownloadedAmount() {
        return downloadedAmount.get();
    }

    // Helper classes
    @ExcludeFromGeneratedCoverageReport
    class ProgressPropertyListener implements ChangeListener<Number> {
        @Override
        public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
            if (newVal.doubleValue() == -1) {
                CustomLogger.log(Level.INFO, "New progress value is still -1", DownloadTask.class.getName());
                return;
            }
            downloadedAmount.setValue(newVal.doubleValue() * downloadFileSize);
        }
    }
}
