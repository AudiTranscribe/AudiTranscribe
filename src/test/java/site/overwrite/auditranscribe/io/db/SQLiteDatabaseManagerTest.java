/*
 * SQLiteDatabaseManagerTest.java
 *
 * Created on 2022-05-11
 * Updated on 2022-07-02
 *
 * Description: Test `SQLiteDatabaseManager.java`.
 */

package site.overwrite.auditranscribe.io.db;

import org.junit.jupiter.api.*;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SQLiteDatabaseManagerTest {
    // Test variables
    SQLiteDatabaseManager manager;

    String createTableQuery1 = """
            CREATE TABLE IF NOT EXISTS "TestTable1" (
                "id"	INTEGER,
                "field1"	INTEGER NOT NULL UNIQUE,
                "field2"	TEXT,
                "field3"	REAL UNIQUE,
                PRIMARY KEY("id")
            );
            """;
    String createTableQuery2 = """
            CREATE TABLE IF NOT EXISTS "TestTable2" (
                "id"	INTEGER,
                "field1"	INTEGER NOT NULL UNIQUE,
                "field2"	TEXT,
                "field3"	REAL UNIQUE,
                PRIMARY KEY("id")
            );
            """;

    String dropTableQuery1 = """
            DROP TABLE "TestTable1";
            """;
    String dropTableQuery2 = """
            DROP TABLE "TestTable2";
            """;

    String insertOneEntry1 = """
            INSERT INTO "TestTable1" ("field1", "field2", "field3")
            VALUES (1, "Text1", 0.1);
            """;
    String insertOneEntry2 = """
            INSERT INTO "TestTable2" ("field1", "field2", "field3")
            VALUES (?, ?, ?);
            """;

    String insertMultipleEntries = """
            INSERT INTO "TestTable1" ("field1", "field2", "field3")
            VALUES
                (2, "Text2", 0.2),
                (3, "Text3", 0.3),
                (4, "Text4", 0.4),
                (5, "Text5", 0.5);
            """;

    String getEntries1 = """
            SELECT * FROM "TestTable1";
            """;
    String getEntries2 = """
            SELECT * FROM "TestTable2"
            WHERE "field2" = ?;
            """;

    // Tests
    @Test
    @Order(1)
    void setUpDatabase() throws SQLException {
        // Set up the SQLite database manager for the tests
        manager = new SQLiteDatabaseManager(
                IOMethods.joinPaths(
                        IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                        "testing-files", "database", "test-database.db"
                )
        );

        // Connect to the database
        manager.dbConnect();
    }

    @Test
    @Order(2)
    void executeUpdate1() throws SQLException {
        // Test creation of a new table
        manager.executeUpdate(createTableQuery1);

        // Try adding one entry into the test database
        manager.executeUpdate(insertOneEntry1);

        // Now add multiple entries in one shot
        manager.executeUpdate(insertMultipleEntries);
    }

    @Test
    @Order(3)
    void executeGetQuery1() throws SQLException {
        // Get all entries within the table
        int i = 1;

        try (ResultSet resultSet = manager.executeGetQuery(getEntries1)) {
            while (resultSet.next()) {
                assertEquals(i, resultSet.getInt("field1"));
                assertEquals("Text" + i, resultSet.getString("field2"));
                assertEquals(0.1 * i, resultSet.getDouble("field3"), 1e-7);
                i++;
            }
        }
    }

    @Test
    @Order(4)
    void dropTestTable1() throws SQLException {
        // Drop the test table so that the next test can try and create it again
        manager.executeUpdate(dropTableQuery1);
    }

    @Test
    @Order(5)
    void executeUpdate2() throws SQLException {
        // Test creation of a new table
        manager.executeUpdate(createTableQuery2);

        // Try adding an entry into the database using a prepared statement
        try (PreparedStatement preparedStatement = manager.prepareStatement(insertOneEntry2)) {
            // Prepare the statement for execution
            preparedStatement.setInt(1, 100);
            preparedStatement.setString(2, "Hello World!");
            preparedStatement.setFloat(3, 1234.5678f);

            // Execute the statement
            manager.executeUpdate(preparedStatement);
        }

        // Try adding one more
        try (PreparedStatement preparedStatement = manager.prepareStatement(insertOneEntry2)) {
            // Prepare the statement for execution
            preparedStatement.setInt(1, 101);
            preparedStatement.setString(2, "Goodbye!");
            preparedStatement.setFloat(3, 2345.6789f);

            // Execute the statement
            manager.executeUpdate(preparedStatement);
        }
    }

    @Test
    @Order(6)
    void executeGetQuery2() throws SQLException {
        try (PreparedStatement preparedStatement = manager.prepareStatement(getEntries2)) {
            // Prepare the statement for execution
            preparedStatement.setString(1, "Hello World!");

            // Execute the statement
            try (ResultSet resultSet = manager.executeGetQuery(preparedStatement)) {
                resultSet.next();
                assertEquals(100, resultSet.getInt("field1"));
                assertEquals("Hello World!", resultSet.getString("field2"));
                assertEquals(1234.5678f, resultSet.getFloat("field3"));
            }
        }
    }

    @Test
    @Order(7)
    void dropTestTable2() throws SQLException {
        // Drop the test table so that the next test can try and create it again
        manager.executeUpdate(dropTableQuery2);
    }

    @Test
    @Order(8)
    void closeConnection() throws SQLException {
        // Must close connection to the database
        manager.dbDisconnect();
    }
}