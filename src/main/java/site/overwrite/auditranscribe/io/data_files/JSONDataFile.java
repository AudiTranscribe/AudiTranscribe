/*
 * JSONDataFile.java
 *
 * Created on 2022-05-30
 * Updated on 2022-08-13
 *
 * Description: Abstract class that helps handle interactions with JSON files.
 */

package site.overwrite.auditranscribe.io.data_files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import site.overwrite.auditranscribe.exceptions.io.FailedToMakeJSONFileException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Abstract class that helps handle interactions with JSON files.
 */
public abstract class JSONDataFile<T> {
    // Attributes
    public final String filePath;

    public Class<T> cls;

    public T data;

    /**
     * Initialization method for a <code>JSONDataFile</code> object.
     *
     * @param filePath <b>Absolute</b> file path to the JSON file.
     * @param cls      Class to use.
     */
    public JSONDataFile(String filePath, Class<T> cls) {
        // Update attributes
        this.cls = cls;
        this.filePath = filePath;

        // Create the GSON object
        Gson gson = new Gson();

        try (Reader reader = new FileReader(this.filePath)) {
            // Try loading the settings data
            data = gson.fromJson(reader, cls);

        } catch (FileNotFoundException e) {
            try {
                createNewFile();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e1) {
                throw new FailedToMakeJSONFileException(e1);
            }
        } catch (IOException | JsonSyntaxException e) {
            throw new JsonIOException(e);
        }
    }

    // Public methods

    /**
     * Method that saves the current settings data to the settings file.
     */
    public void saveFile() {
        // Create the GSON object
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer writer = new FileWriter(filePath)) {
            // Try writing to the settings data file
            gson.toJson(data, writer);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Private methods

    /**
     * Helper method that creates a new settings file.
     */
    private void createNewFile() throws NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException {
        // Initialize a new `SettingsData` object with no data inside it
        data = cls.getConstructor().newInstance();

        // Write that data to the settings file
        saveFile();
    }
}
