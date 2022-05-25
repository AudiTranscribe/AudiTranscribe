/*
 * CustomTask.java
 *
 * Created on 2022-05-25
 * Updated on 2022-05-25
 *
 * Description: A custom task class where some protected methods are made public.
 */

package site.overwrite.auditranscribe;

import javafx.concurrent.Task;

public abstract class CustomTask<V> extends Task<V> {
    @Override
    public void updateProgress(long current, long total) {
        super.updateProgress(current, total);
    }

    @Override
    public void updateProgress(double current, double total) {
        super.updateProgress(current, total);
    }

    @Override
    abstract protected V call() throws Exception;
}
