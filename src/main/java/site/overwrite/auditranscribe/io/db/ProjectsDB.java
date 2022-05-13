/*
 * ProjectsDB.java
 *
 * Created on 2022-05-11
 * Updated on 2022-05-14
 *
 * Description: Class that interfaces with the projects' database.
 */

package site.overwrite.auditranscribe.io.db;

import org.javatuples.Pair;
import site.overwrite.auditranscribe.io.IOMethods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that interfaces with the projects' database.
 */
public class ProjectsDB {
    // SQL Queries
    public static String SQL_CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS "Projects" (
                "id"		INTEGER,
            	"filepath"	TEXT UNIQUE,
            	"filename"	TEXT NOT NULL,
            	PRIMARY KEY("id")
            );
            """;
    public static String SQL_GET_ALL_PROJECTS = """
            SELECT * FROM "Projects";
            """;
    public static String SQL_CHECK_IF_PROJECT_EXISTS = """
            SELECT "id" FROM "Projects"
            WHERE "filepath" = ?;
            """;
    public static String SQL_INSERT_PROJECT_RECORD = """
            INSERT INTO "Projects" ("filepath", "filename")
            VALUES (?, ?);
            """;
    public static String SQL_UPDATE_PROJECT_RECORD = """
            UPDATE "Projects" SET
                "filepath" = ?,
                "filename" = ?
            WHERE "id" = ?;
            """;
    public static String SQL_DELETE_PROJECT_RECORD = """
            DELETE FROM "Projects"
            WHERE "id" = ?;
            """;

    // Constants
    public static String PROJECT_FILE_LIST_DB_NAME = "Projects.db";
    public static String PROJECT_FILE_LIST_DB_PATH = IOMethods.APP_DATA_FOLDER_PATH_STRING + PROJECT_FILE_LIST_DB_NAME;

    // Attributes
    final SQLiteDatabaseManager dbManager;

    /**
     * Initialization method for a new <code>ProjectsDB</code> object.
     *
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public ProjectsDB() throws SQLException {
        // Attempt creation of the database file
        IOMethods.createFile(PROJECT_FILE_LIST_DB_PATH);

        // Create a database manager object
        dbManager = new SQLiteDatabaseManager(PROJECT_FILE_LIST_DB_PATH);

        // Create the projects table
        dbManager.dbConnect();
        dbManager.executeUpdate(SQL_CREATE_TABLE);
        dbManager.dbDisconnect();
    }

    // Public methods

    /**
     * Method that gets all the projects' details.
     *
     * @return Map of all the projects. Key is the database primary key and values are the filepath
     * and filename in that order.
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public Map<Integer, Pair<String, String>> getAllProjects() throws SQLException {
        // Start connection with the database
        dbManager.dbConnect();

        // Process data
        Map<Integer, Pair<String, String>> allProjects = new HashMap<>();

        try (ResultSet resultSet = dbManager.executeGetQuery(SQL_GET_ALL_PROJECTS)) {
            while (resultSet.next()) {  // Still have next entry
                // Get the data
                int pk = resultSet.getInt("id");
                String filepath = resultSet.getString("filepath");
                String filename = resultSet.getString("filename");

                // Place the data into the map
                allProjects.put(pk, new Pair<>(filepath, filename));
            }
        }

        // Close connection with database
        dbManager.dbDisconnect();

        // Return the map
        return allProjects;
    }

    /**
     * Method that checks if the project with the specified file path already exists within the
     * database.
     *
     * @param filepath <b>Absolute</b> path to the project file.
     * @return Boolean. Is <code>true</code> if the project already exists, and <code>false</code>
     * if it does not.
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public boolean checkIfProjectExists(String filepath) throws SQLException {
        // Start connection with the database
        dbManager.dbConnect();

        boolean projectExists = false;
        try (PreparedStatement checkProjectStatement = dbManager.prepareStatement(SQL_CHECK_IF_PROJECT_EXISTS)) {
            // Prepare the statement for execution
            checkProjectStatement.setString(1, filepath);

            // Execute the statement
            try (ResultSet resultSet = dbManager.executeGetQuery(checkProjectStatement)) {
                if (resultSet.next()) projectExists = true;
            }
        }

        // Close connection with database
        dbManager.dbDisconnect();

        // Return the `projectExists` flag
        return projectExists;
    }

    /**
     * Method that inserts a new project record into the database.
     *
     * @param filepath <b>Absolute</b> path to the project file.
     * @param filename Project file's name.
     * @throws SQLException If something went wrong when executing the SQL query.<br>
     *                      Specifically, for this method, a <code>SQLException</code> would likely
     *                      be due to a <code>UNIQUE</code> constraint failure; i.e. there already
     *                      exists a project file with the same file path as the current record.
     */
    public void insertProjectRecord(String filepath, String filename) throws SQLException {
        // Start connection with the database
        dbManager.dbConnect();

        try (PreparedStatement insertProjectStatement = dbManager.prepareStatement(SQL_INSERT_PROJECT_RECORD)) {
            // Prepare the statement for execution
            insertProjectStatement.setString(1, filepath);
            insertProjectStatement.setString(2, filename);

            // Execute the statement
            dbManager.executeUpdate(insertProjectStatement);
        }

        // Close connection with database
        dbManager.dbDisconnect();
    }

    /**
     * Method that updates a specific project's record.
     *
     * @param key      Public key of the record in the database.
     * @param filepath <b>Absolute </b> path to the project file.
     * @param filename Project file's name.
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public void updateProjectRecord(int key, String filepath, String filename) throws SQLException {
        // Start connection with the database
        dbManager.dbConnect();

        try (PreparedStatement updateProjectStatement = dbManager.prepareStatement(SQL_UPDATE_PROJECT_RECORD)) {
            // Prepare the statement for execution
            updateProjectStatement.setString(1, filepath);
            updateProjectStatement.setString(2, filename);
            updateProjectStatement.setInt(3, key);

            // Execute the statement
            dbManager.executeUpdate(updateProjectStatement);
        }

        // Close connection with database
        dbManager.dbDisconnect();
    }

    /**
     * Method that deletes a specific project's record from the database.
     *
     * @param key Public key of the record in the database.
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public void deleteProjectRecord(int key) throws SQLException {
        // Start connection with the database
        dbManager.dbConnect();

        try (PreparedStatement deleteProjectStatement = dbManager.prepareStatement(SQL_DELETE_PROJECT_RECORD)) {
            // Prepare the statement for execution
            deleteProjectStatement.setInt(1, key);

            // Execute the statement
            dbManager.executeUpdate(deleteProjectStatement);
        }

        // Close connection with database
        dbManager.dbDisconnect();
    }
}
