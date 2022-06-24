/*
 * ProjectsDB.java
 *
 * Created on 2022-05-11
 * Updated on 2022-06-24
 *
 * Description: Class that interfaces with the projects' database.
 */

package site.overwrite.auditranscribe.io.db;

import org.javatuples.Pair;
import site.overwrite.auditranscribe.io.IOConstants;
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
    public static String SQL_GET_ID_OF_PROJECT_WITH_FILEPATH = """
            SELECT "id" FROM "Projects"
            WHERE "filepath" = ?;
            """;
    public static String SQL_CHECK_IF_PROJECT_EXISTS = """
            SELECT "id" FROM "Projects"
            WHERE "filepath" = ?;
            """;
    public static String SQL_INSERT_PROJECT_RECORD = """
            INSERT INTO "Projects" ("filepath", "filename")
            VALUES (?, ?);
            """;
    public static String SQL_DELETE_PROJECT_RECORD = """
            DELETE FROM "Projects"
            WHERE "id" = ?;
            """;

    // Constants
    public static String PROJECT_FILE_LIST_DB_NAME = "Projects.db";
    public static String PROJECT_FILE_LIST_DB_PATH = IOMethods.joinPaths(
            IOConstants.APP_DATA_FOLDER_PATH, PROJECT_FILE_LIST_DB_NAME
    );

    // Attributes
    final SQLiteDatabaseManager dbManager;

    /**
     * Initialization method for a new <code>ProjectsDB</code> object.
     *
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public ProjectsDB() throws SQLException {
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
     * Method that gets the primary key of a project with a specified file path.
     *
     * @param filepath <b>Absolute</b> file path to the project file.
     * @return An integer, representing the primary key of the project in the projects' database.
     * Returns <code>-1</code> if the project is not present in the database.
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public int getIDOfProjectWithFilepath(String filepath) throws SQLException {
        // Start connection with the database
        dbManager.dbConnect();

        // Query the database for the PK of the project
        int pk = -1;

        try (PreparedStatement getIDStatement = dbManager.prepareStatement(SQL_GET_ID_OF_PROJECT_WITH_FILEPATH)) {
            // Prepare the statement for execution
            getIDStatement.setString(1, filepath);

            // Execute the statement
            try (ResultSet resultSet = dbManager.executeGetQuery(getIDStatement)) {
                if (resultSet.next()) pk = resultSet.getInt("id");
            }
        }

        // Close connection with database
        dbManager.dbDisconnect();

        // Return the found primary key
        return pk;
    }

    /**
     * Method that checks if the project with the specified file path does not exist within the
     * database.
     *
     * @param filepath <b>Absolute</b> path to the project file.
     * @return Boolean. Is <code>true</code> if the project does not exist, and <code>false</code>
     * if it is already present in the database.
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public boolean checkIfProjectDoesNotExist(String filepath) throws SQLException {
        // Start connection with the database
        dbManager.dbConnect();

        boolean projectDoesNotExist = true;
        try (PreparedStatement checkProjectStatement = dbManager.prepareStatement(SQL_CHECK_IF_PROJECT_EXISTS)) {
            // Prepare the statement for execution
            checkProjectStatement.setString(1, filepath);

            // Execute the statement
            try (ResultSet resultSet = dbManager.executeGetQuery(checkProjectStatement)) {
                if (resultSet.next()) projectDoesNotExist = false;
            }
        }

        // Close connection with database
        dbManager.dbDisconnect();

        // Return the `projectDoesNotExist` flag
        return projectDoesNotExist;
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
