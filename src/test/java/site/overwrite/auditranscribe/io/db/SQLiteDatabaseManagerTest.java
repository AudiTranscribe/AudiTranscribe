/*
 * SQLiteDatabaseManagerTest.java
 *
 * Created on 2022-05-11
 * Updated on 2022-06-23
 *
 * Description: Test `SQLiteDatabaseManager.java`.
 */

package site.overwrite.auditranscribe.io.db;

import org.junit.jupiter.api.*;
import site.overwrite.auditranscribe.io.IOConstants;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.ResultSet;
import java.sql.SQLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SQLiteDatabaseManagerTest {
    // Test variables
    SQLiteDatabaseManager manager;

    String createTableQuery = """
            CREATE TABLE IF NOT EXISTS "TestTable" (
                "id"	INTEGER,
                "field1"	INTEGER NOT NULL UNIQUE,
                "field2"	TEXT,
                "field3"	REAL UNIQUE,
                PRIMARY KEY("id")
            );
            """;

    String dropTableQuery = """
            DROP TABLE "TestTable";
            """;

    String insertOneEntry = """
            INSERT INTO "TestTable" ("field1", "field2", "field3")
            VALUES (1, "Text1", 0.1);
            """;

    String insertMultipleEntries = """
            INSERT INTO "TestTable" ("field1", "field2", "field3")
            VALUES
                (2, "Text2", 0.2),
                (3, "Text3", 0.3),
                (4, "Text4", 0.4),
                (5, "Text5", 0.5);
            """;

    String getEntries = """
            SELECT * FROM "TestTable";
            """;

    // Tests
    @Test
    @Order(1)
    void setUpDatabase() {
        // Set up the SQLite database manager for the tests
        manager = new SQLiteDatabaseManager(
                IOConstants.RESOURCES_FOLDER_PATH_STRING +
                        "tests-output-directory" + IOConstants.SEPARATOR +
                        "database" + IOConstants.SEPARATOR +
                        "test-database.db"
        );

        // Connect to the database
        manager.dbConnect();
    }

    @Test
    @Order(2)
    void executeUpdate() throws SQLException {
        // Test creation of a new table
        manager.executeUpdate(createTableQuery);

        // Try adding one entry into the test database
        manager.executeUpdate(insertOneEntry);

        // Now add multiple entries in one shot
        manager.executeUpdate(insertMultipleEntries);
    }

    @Test
    @Order(3)
    void executeGetQuery() throws SQLException {
        // Get all entries within the table
        int i = 1;

        try (ResultSet resultSet = manager.executeGetQuery(getEntries)) {
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
    void dropTestTable() throws SQLException {
        // Drop the test table so that the next test can try and create it again
        manager.executeUpdate(dropTableQuery);
    }

    @Test
    @Order(5)
    void closeConnection() {
        // Must close connection to the database
        manager.dbDisconnect();
    }
}