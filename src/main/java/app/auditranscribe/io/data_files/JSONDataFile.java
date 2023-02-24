/*
 * JSONDataFile.java
 * Description: Abstract class that helps handle interactions with JSON files.
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

package app.auditranscribe.io.data_files;

import app.auditranscribe.io.exceptions.FailedToCreateJSONFileException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Abstract class that helps handle interactions with JSON files.
 *
 * @param <T> Class that stores the data from the JSON file.
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
     * @param cls      Class that stores the data from the JSON file.
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
                throw new FailedToCreateJSONFileException(e1);
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

        // Try writing to the settings data file
        try (Writer writer = new FileWriter(filePath)) {
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
