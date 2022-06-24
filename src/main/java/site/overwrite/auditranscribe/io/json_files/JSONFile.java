/*
 * JSONFile.java
 *
 * Created on 2022-05-30
 * Updated on 2022-06-24
 *
 * Description: Handles interactions with JSON files.
 */

package site.overwrite.auditranscribe.io.json_files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Handles interactions with JSON files.
 */
public abstract class JSONFile<T> {
    // Attributes
    public final String filePath;

    public Class<T> cls;

    public T data;

    /**
     * Initialization method for a <code>JSONFile</code> object.
     *
     * @param fileName File name of for the new JSON file. <b>Includes the extension</b>.
     * @param cls      Class to use.
     */
    public JSONFile(String fileName, Class<T> cls) {
        // Update attributes
        this.cls = cls;
        filePath = IOMethods.joinPaths(IOConstants.APP_DATA_FOLDER_PATH_STRING, fileName);

        // Create the GSON object
        Gson gson = new Gson();

        try (Reader reader = new FileReader(filePath)) {
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
