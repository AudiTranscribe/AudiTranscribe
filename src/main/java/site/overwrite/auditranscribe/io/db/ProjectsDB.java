/*
 * ProjectsDB.java
 * Description: Class that interfaces with the projects' database.
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

package site.overwrite.auditranscribe.io.db;

import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.tuples.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that interfaces with the projects' database.
 */
public class ProjectsDB {
    // Constants
    public static int SQL_DATABASE_VERSION = 0x00070001;  // Database version 0.7.0, revision 1 -> 00 07 00 01

    // SQL Queries
    public static String SQL_CREATE_PROJECTS_TABLE = """
            CREATE TABLE IF NOT EXISTS "Projects" (
                "id"			INTEGER,
            	"filepath"		TEXT UNIQUE,
            	"project_name"	TEXT NOT NULL,
            	PRIMARY KEY("id")
            );
            """;
    public static String SQL_CREATE_VERSION_TABLE = """
            CREATE TABLE IF NOT EXISTS "Version" (
            	"version_number"	INTEGER NOT NULL
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
            INSERT INTO "Projects" ("filepath", "project_name")
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

        // Start connection with the database
        dbManager.dbConnect();

        // Prepare to create the database tables
        dbManager.executeUpdate(SQL_CREATE_PROJECTS_TABLE);
        dbManager.executeUpdate(SQL_CREATE_VERSION_TABLE);

        // Add version entry
        // (Note: in general, modifying the SQL query like this is insecure and not safe. However, since we control
        // the `SQL_DATABASE_VERSION`, this will be safe.)
        try (ResultSet resultSet = dbManager.executeGetQuery(
                "SELECT COUNT(*) AS X FROM \"Version\";"
        )) {
            if (resultSet.next()) {
                if (resultSet.getInt("X") == 0) {
                    dbManager.executeUpdate(
                            "INSERT INTO \"Version\" VALUES (" + SQL_DATABASE_VERSION + ");"
                    );
                } else {
                    dbManager.executeUpdate(
                            "UPDATE \"Version\" SET version_number = " + SQL_DATABASE_VERSION + ";"
                    );
                }
            }
        }

        // FOR VERSION 0.7.x ONLY: Update existing "Projects" table
        // TODO: IMPORTANT: Remove once 0.7.x passes
        try (ResultSet resultSet = dbManager.executeGetQuery(
                "SELECT COUNT(*) AS X FROM pragma_table_info('Projects') WHERE name='filename';"
        )) {
            if (resultSet.next() && resultSet.getInt("X") == 1) {
                dbManager.executeUpdate("""
                        ALTER TABLE "Projects"
                        RENAME COLUMN "filename" TO "project_name";
                        """);
            }
        }
        try (ResultSet resultSet = dbManager.executeGetQuery(
                "SELECT \"id\", \"project_name\" FROM \"Projects\";"
        )) {
            while (resultSet.next()) {
                // Get the ID
                int id = resultSet.getInt("id");
                String projectName = resultSet.getString("project_name");

                // Update the project name
                if (projectName.substring(projectName.length() - 5).equalsIgnoreCase(".audt")) {
                    projectName = projectName.substring(0, projectName.length() - 5);  // Exclude the ".audt" at the end
                }

                try (PreparedStatement statement = dbManager.prepareStatement(
                        "UPDATE \"Projects\" SET \"project_name\" = ? WHERE \"id\" = ?;"
                )) {
                    statement.setString(1, projectName);
                    statement.setInt(2, id);

                    dbManager.executeUpdate(statement);
                }
            }
        }

        // Close connection with database
        dbManager.dbDisconnect();
    }

    // Public methods

    /**
     * Method that gets all the projects' details.
     *
     * @return Map of all the projects. Key is the database primary key and values are the filepath
     * and project name in that order.
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
                String projectName = resultSet.getString("project_name");

                // Place the data into the map
                allProjects.put(pk, new Pair<>(filepath, projectName));
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
    public int getPKOfProjectWithFilepath(String filepath) throws SQLException {
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
     * @param filepath    <b>Absolute</b> path to the project file.
     * @param projectName Name of the project that is being added.
     * @throws SQLException If something went wrong when executing the SQL query.<br>
     *                      Specifically, for this method, a <code>SQLException</code> would likely
     *                      be due to a <code>UNIQUE</code> constraint failure; i.e. there already
     *                      exists a project file with the same file path as the current record.
     */
    public void insertProjectRecord(String filepath, String projectName) throws SQLException {
        // Start connection with the database
        dbManager.dbConnect();

        try (PreparedStatement insertProjectStatement = dbManager.prepareStatement(SQL_INSERT_PROJECT_RECORD)) {
            // Prepare the statement for execution
            insertProjectStatement.setString(1, filepath);
            insertProjectStatement.setString(2, projectName);

            // Execute the statement
            dbManager.executeUpdate(insertProjectStatement);
        }

        // Close connection with database
        dbManager.dbDisconnect();
    }

    /**
     * Method that deletes a specific project's record from the database.
     *
     * @param key Primary key of the record in the database.
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
