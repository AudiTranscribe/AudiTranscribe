/*
 * JSONFile.java
 *
 * Created on 2022-05-30
 * Updated on 2022-06-20
 *
 * Description: Handles interactions with JSON files.
 */

package site.overwrite.auditranscribe.io.json_files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import site.overwrite.auditranscribe.io.IOConstants;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Handles interactions with JSON files.
 */
public abstract class JSONFile<T> {
    // Constants
    public final String FILE_PATH;

    // Attributes
    public Class<T> cls;

    public T data;

    /**
     * Initialization method for a <code>JSONFile</code> object.
     */
    public JSONFile(String fileName, Class<T> cls) {
        // Update attributes
        this.cls = cls;
        FILE_PATH = IOConstants.APP_DATA_FOLDER_PATH_STRING + fileName;

        // Create the GSON object
        Gson gson = new Gson();

        try (Reader reader = new FileReader(FILE_PATH)) {
            // Try loading the settings data
            data = gson.fromJson(reader, cls);

        } catch (FileNotFoundException e) {
            try {
                createNewFile();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e1) {
                throw new RuntimeException(e1);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Public methods

    /**
     * Method that saves the current settings data to the settings file.
     */
    public void saveFile() {
        // Create the GSON object
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer writer = new FileWriter(FILE_PATH)) {
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
