/*
 * SQLiteDatabaseManager.java
 *
 * Created on 2022-05-11
 * Updated on 2022-07-01
 *
 * Description: Class that helps manage the interactions with an SQLite3 database.
 */

package site.overwrite.auditranscribe.io.db;

import site.overwrite.auditranscribe.io.IOMethods;

import java.sql.*;

public class SQLiteDatabaseManager {
    // Constants
    public static final String ACCESS_METHOD_STRING = "jdbc:sqlite:";
    public static int TIMEOUT = 5;  // In seconds

    // Attributes
    public final String databaseAbsolutePath;

    private Connection connection;
    private Statement statement;

    /**
     * Initialization method for a <code>SQLiteDatabaseManager</code> to access a SQLite3 database.
     *
     * @param databaseAbsolutePath Absolute file path to the SQLite3 database.
     */
    public SQLiteDatabaseManager(String databaseAbsolutePath) {
        // Set path to the database
        this.databaseAbsolutePath = databaseAbsolutePath;

        // Attempt creation of the database file
        IOMethods.createFile(databaseAbsolutePath);
    }

    // Public methods

    /**
     * Method that helps connect to the SQLite3 database.
     *
     * @throws SQLException If a database access error occurs, or the url is <code>null</code>.
     */
    public void dbConnect() throws SQLException {
        // Attempt to connect to the database
        connection = DriverManager.getConnection(ACCESS_METHOD_STRING + databaseAbsolutePath);

        // Create statement and set timeout
        statement = connection.createStatement();
        statement.setQueryTimeout(TIMEOUT);
    }

    /**
     * Method that closes the connection to the SQLite3 database.
     *
     * @throws SQLException If a database access error occurs, or the url is <code>null</code>.
     */
    public void dbDisconnect() throws SQLException {
        // Attempt to close the connection
        connection.close();

        // Set both `connection` and `statement` to `null` to signal that it is no longer connected
        connection = null;
        statement = null;
    }

    /**
     * Helper method that prepares an SQL statement.
     *
     * @param sqlStatement Statement to be prepared.
     * @return Prepared SQL statement. (aka parameterized query)
     * @throws SQLException If the SQL statement has an error.
     */
    public PreparedStatement prepareStatement(String sqlStatement) throws SQLException {
        return connection.prepareStatement(sqlStatement);
    }

    /**
     * Method that executes an <em>update-like</em> SQL statement.
     *
     * @param sqlStatement Statement to execute.
     * @throws SQLException If the SQL statement has an error, or if the execution of the SQL
     *                      statement encounters an error.
     */
    public void executeUpdate(String sqlStatement) throws SQLException {
        statement.executeUpdate(sqlStatement);
    }

    /**
     * Method that executes an <em>update-like</em> SQL statement.
     *
     * @param preparedStatement Prepared statement to execute.
     * @throws SQLException If the SQL statement has an error, or if the execution of the SQL
     *                      statement encounters an error.
     */
    public void executeUpdate(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.executeUpdate();
    }


    /**
     * Method that executes an <em>get-like</em> SQL statement. This retrieves data from the
     * database.
     *
     * @param sqlStatement Statement to execute.
     * @return A <code>ResultSet</code> object containing the results of the query.
     * @throws SQLException If the SQL statement has an error, or if the execution of the SQL
     *                      statement encounters an error.
     */
    public ResultSet executeGetQuery(String sqlStatement) throws SQLException {
        return statement.executeQuery(sqlStatement);
    }

    /**
     * Method that executes an <em>get-like</em> SQL statement. This retrieves data from the
     * database.
     *
     * @param preparedStatement Prepared statement to execute.
     * @return A <code>ResultSet</code> object containing the results of the query.
     * @throws SQLException If the SQL statement has an error, or if the execution of the SQL
     *                      statement encounters an error.
     */
    public ResultSet executeGetQuery(PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeQuery();
    }
}
