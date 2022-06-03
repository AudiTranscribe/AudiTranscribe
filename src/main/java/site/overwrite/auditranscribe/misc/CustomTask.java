/*
 * CustomTask.java
 *
 * Created on 2022-05-25
 * Updated on 2022-06-03
 *
 * Description: A custom task class where some protected methods are made public.
 */

package site.overwrite.auditranscribe.misc;

import javafx.concurrent.Task;

public abstract class CustomTask<V> extends Task<V> {
    // Public methods

    /**
     * Alias to {@link #updateMessage(String)}.
     *
     * @param s The message to set.
     */
    public void setMessage(String s) {
        updateMessage(s);
    }

    // Overwritten methods
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
