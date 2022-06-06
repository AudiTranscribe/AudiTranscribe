/*
 * StreamGobbler.java
 *
 * Created on 2022-06-03
 * Updated on 2022-06-03
 *
 * Description: Class that hooks into the input and output streams of any process.
 */

package site.overwrite.auditranscribe.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * Class that hooks into the input and output streams of any process.
 */
public class StreamGobbler implements Runnable {
    private final InputStream inputStream;
    private final Consumer<String> consumer;

    /**
     * Initialization method for the stream gobbler.
     * @param inputStream   The input stream of the process.
     * @param consumer    The consumer that will consume the stream.
     */
    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
        this.inputStream = inputStream;
        this.consumer = consumer;
    }

    // Overwritten methods
    @Override
    public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
    }
}
