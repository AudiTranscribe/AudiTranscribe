/*
 * ProjectsDB.java
 * Description: Manages the interactions with the projects database.
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

package app.auditranscribe.io.db;

import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the interactions with the projects database.
 */
@ExcludeFromGeneratedCoverageReport
public class ProjectsDB {
    // Constants
    public static String PROJECTS_DB_NAME = "Projects.db";
    public static String PROJECTS_DB_PATH = IOMethods.joinPaths(
            IOConstants.APP_DATA_FOLDER_PATH, PROJECTS_DB_NAME
    );
    public static int PROJECTS_DB_VERSION = 0x000B0001;  // Database version 0.11.0, revision 1 -> 00 0B 00 01

    // SQL Queries
    static String SQL_CREATE_PROJECTS_TABLE = """
            CREATE TABLE IF NOT EXISTS "Projects" (
                "id"			INTEGER,
            	"filepath"		TEXT UNIQUE,
            	"project_name"	TEXT NOT NULL,
            	PRIMARY KEY("id")
            );
            """;
    static String SQL_CREATE_VERSION_TABLE = """
            CREATE TABLE IF NOT EXISTS "Version" (
            	"version_number"	INTEGER NOT NULL
            );
            """;
    static String SQL_GET_ALL_PROJECTS = """
            SELECT * FROM "Projects";
            """;
    static String SQL_GET_ID_OF_PROJECT_WITH_FILEPATH = """
            SELECT "id" FROM "Projects"
            WHERE "filepath" = ?;
            """;
    static String SQL_CHECK_IF_PROJECT_EXISTS = """
            SELECT "id" FROM "Projects"
            WHERE "filepath" = ?;
            """;
    static String SQL_INSERT_PROJECT_RECORD = """
            INSERT INTO "Projects" ("filepath", "project_name")
            VALUES (?, ?);
            """;
    static String SQL_DELETE_PROJECT_RECORD = """
            DELETE FROM "Projects"
            WHERE "id" = ?;
            """;
    static String SQL_UPDATE_PROJECT_NAME = """
            UPDATE "Projects" SET
            	"project_name" = ?
            WHERE "filepath" = ?;
            """;

    // Attributes
    final SQLiteDatabaseManager dbManager;

    /**
     * Initialization method for a new <code>ProjectsDB</code> object.
     *
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public ProjectsDB() throws SQLException {
        dbManager = new SQLiteDatabaseManager(PROJECTS_DB_PATH);
        dbManager.dbConnect();

        // Prepare to create the database tables
        dbManager.executeUpdate(SQL_CREATE_PROJECTS_TABLE);
        dbManager.executeUpdate(SQL_CREATE_VERSION_TABLE);

        // Add version entry
        // (Note: in general, modifying the SQL query like this is insecure and not safe. However, since we control
        // the `SQL_DATABASE_VERSION`, this will be safe.)
        try (ResultSet resultSet = dbManager.executeGetQuery(
                "SELECT COUNT(*) AS n FROM \"Version\";"
        )) {
            if (resultSet.next()) {
                if (resultSet.getInt("n") == 0) {
                    dbManager.executeUpdate(
                            "INSERT INTO \"Version\" VALUES (" + PROJECTS_DB_VERSION + ");"
                    );
                } else {
                    dbManager.executeUpdate(
                            "UPDATE \"Version\" SET version_number = " + PROJECTS_DB_VERSION + ";"
                    );
                }
            }
        }

        // Once all updates are done we disconnect
        dbManager.dbDisconnect();
    }

    // Public methods

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
        dbManager.dbConnect();

        // Insert project record into database
        try (PreparedStatement insertProjectStatement = dbManager.prepareStatement(SQL_INSERT_PROJECT_RECORD)) {
            insertProjectStatement.setString(1, filepath);
            insertProjectStatement.setString(2, projectName);

            dbManager.executeUpdate(insertProjectStatement);
        }

        // Once insert is complete we disconnect
        dbManager.dbDisconnect();
    }

    /**
     * Method that gets all the projects' details.
     *
     * @return Map of all the projects.<br>
     * Key is the database primary key. Values are the filepath and project name in that order.
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public Map<Integer, Pair<String, String>> getAllProjects() throws SQLException {
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

        // Once retrieval is complete we disconnect
        dbManager.dbDisconnect();
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
        dbManager.dbConnect();

        // Query the database for the PK of the project
        int pk = -1;

        try (PreparedStatement getIDStatement = dbManager.prepareStatement(SQL_GET_ID_OF_PROJECT_WITH_FILEPATH)) {
            getIDStatement.setString(1, filepath);

            try (ResultSet resultSet = dbManager.executeGetQuery(getIDStatement)) {
                if (resultSet.next()) pk = resultSet.getInt("id");
            }
        }

        // Once retrieval is complete we disconnect
        dbManager.dbDisconnect();
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
        dbManager.dbConnect();

        // Query database for existence of the project with the given filepath
        boolean projectDoesNotExist = true;
        try (PreparedStatement checkProjectStatement = dbManager.prepareStatement(SQL_CHECK_IF_PROJECT_EXISTS)) {
            checkProjectStatement.setString(1, filepath);

            try (ResultSet resultSet = dbManager.executeGetQuery(checkProjectStatement)) {
                if (resultSet.next()) projectDoesNotExist = false;
            }
        }

        // Once retrieval is complete we disconnect
        dbManager.dbDisconnect();
        return projectDoesNotExist;
    }

    /**
     * Method that updates a specific project's project name in the database.
     *
     * @param filepath       <b>Absolute</b> path to the project file.
     * @param newProjectName New project name to rename the project to.
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public void updateProjectName(String filepath, String newProjectName) throws SQLException {
        dbManager.dbConnect();

        // Update the project's name
        try (PreparedStatement updateProjectNameStatement = dbManager.prepareStatement(SQL_UPDATE_PROJECT_NAME)) {
            updateProjectNameStatement.setString(1, newProjectName);
            updateProjectNameStatement.setString(2, filepath);

            dbManager.executeUpdate(updateProjectNameStatement);
        }

        // Once update is complete we disconnect
        dbManager.dbDisconnect();
    }

    /**
     * Method that deletes a specific project's record from the database.
     *
     * @param key Primary key of the record in the database.
     * @throws SQLException If something went wrong when executing the SQL query.
     */
    public void deleteProjectRecord(int key) throws SQLException {
        dbManager.dbConnect();

        // Find and delete the record with the given key
        try (PreparedStatement deleteProjectStatement = dbManager.prepareStatement(SQL_DELETE_PROJECT_RECORD)) {
            deleteProjectStatement.setInt(1, key);

            dbManager.executeUpdate(deleteProjectStatement);
        }

        // Once deletion is complete we disconnect
        dbManager.dbDisconnect();
    }
}
