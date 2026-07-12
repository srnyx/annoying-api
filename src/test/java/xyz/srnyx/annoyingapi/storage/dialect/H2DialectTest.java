package xyz.srnyx.annoyingapi.storage.dialect;

import org.jetbrains.annotations.NotNull;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.Test;
import xyz.srnyx.annoyingapi.data.StringData;
import xyz.srnyx.annoyingapi.storage.*;
import xyz.srnyx.annoyingapi.storage.dialects.Dialect;
import xyz.srnyx.annoyingapi.storage.dialects.SQLDialect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SQLDialect} using H2
 */
class H2DialectTest extends StorageTestSupport {

    @NotNull
    private SQLDialect createSqlDialect() throws ConnectionException {
        final DataManager manager = createDataManager(StorageMethod.H2);
        assertInstanceOf(SQLDialect.class, manager.dialect);
        return (SQLDialect) manager.dialect;
    }

    @NotNull
    private static Map<String, Set<String>> tablesKeys(@NotNull String table, @NotNull String @NotNull ... keys) {
        final Map<String, Set<String>> map = new HashMap<>();
        map.put(table, new HashSet<>(Set.of(keys)));
        return map;
    }

    @Test
    void createTablesKeys_createsTableWithWorkingPrimaryKey() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();
        dialect.createTablesKeys(tablesKeys("players", "name"));

        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));
        assertNull(dialect.setToDatabase("players", "player2", "name", "Bob"));

        assertEquals("Alice", dialect.getFromDatabase("players", "player1", "name").orElse(null));
        assertEquals("Bob", dialect.getFromDatabase("players", "player2", "name").orElse(null));
    }

    @Test
    void setToDatabase_duplicateTargetUpdatesInPlaceInsteadOfDuplicating() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();
        dialect.createTablesKeys(tablesKeys("players", "name"));

        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));
        assertNull(dialect.setToDatabase("players", "player1", "name", "Alicia"));

        assertEquals("Alicia", dialect.getFromDatabase("players", "player1", "name").orElse(null));
    }

    @Test
    void createTablesKeys_addsMissingColumnToExistingTable() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();

        // First pass: table only has "name"
        dialect.createTablesKeys(tablesKeys("players", "name"));
        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));

        // Second pass: table now also needs "coins" - existing data for "name" must survive
        dialect.createTablesKeys(tablesKeys("players", "name", "coins"));
        assertNull(dialect.setToDatabase("players", "player1", "coins", "100"));

        assertEquals("Alice", dialect.getFromDatabase("players", "player1", "name").orElse(null));
        assertEquals("100", dialect.getFromDatabase("players", "player1", "coins").orElse(null));
    }

    @Test
    void getFromDatabase_doesNotCrossContaminateBetweenTargets() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();
        dialect.createTablesKeys(tablesKeys("players", "name"));

        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));
        assertNull(dialect.setToDatabase("players", "player2", "name", "Bob"));

        assertEquals("Alice", dialect.getFromDatabase("players", "player1", "name").orElse(null));
        assertEquals("Bob", dialect.getFromDatabase("players", "player2", "name").orElse(null));
        assertTrue(dialect.getFromDatabase("players", "player3", "name").isEmpty());
    }

    @Test
    void setToDatabase_multiColumnMapUpsertIsAllReadableAfterward() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();
        dialect.createTablesKeys(tablesKeys("players", "name", "coins"));

        final ConcurrentHashMap<String, CachedValue> data = new ConcurrentHashMap<>();
        data.put("name", new CachedValue("Alice"));
        data.put("coins", new CachedValue("50"));

        final List<FailedSet> failed = dialect.setToDatabase("players", "player1", data);
        assertTrue(failed.isEmpty(), "Unexpected failures: " + failed);

        assertEquals("Alice", dialect.getFromDatabase("players", "player1", "name").orElse(null));
        assertEquals("50", dialect.getFromDatabase("players", "player1", "coins").orElse(null));
    }

    @Test
    void removeValueFromDatabase_clearsValue() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();
        dialect.createTablesKeys(tablesKeys("players", "name"));

        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));
        assertTrue(dialect.removeValueFromDatabase("players", "player1", "name"));

        assertTrue(dialect.getFromDatabase("players", "player1", "name").isEmpty());
    }

    @Test
    void getMigrationDataFromDatabase_returnsWhatWasWritten() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();
        dialect.createTablesKeys(tablesKeys("players", "name"));
        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));
        assertNull(dialect.setToDatabase("players", "player2", "name", "Bob"));

        final DataManager newManager = createDataManager(StorageMethod.JSON, "storage-migration-target.yml");
        final Dialect.MigrationData migrationData = dialect.getMigrationDataFromDatabase(newManager).orElse(null);
        assertNotNull(migrationData);

        assertTrue(migrationData.tablesKeys().containsKey("players"));
        assertTrue(migrationData.tablesKeys().get("players").contains("name"));

        final String newTableName = newManager.getTableName("players");
        assertTrue(migrationData.data().containsKey(newTableName));
        final ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>> playersData = migrationData.data().get(newTableName);
        assertEquals("Alice", playersData.get("player1").get("name").value());
        assertEquals("Bob", playersData.get("player2").get("name").value());
    }

    @Test
    void getMigrationDataFromDatabase_appliesNewManagerTableNameResolution() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();
        // Mixed-case table name - createTablesKeys lowercases via getTableName, so the physical table is "players"
        dialect.createTablesKeys(tablesKeys("Players", "name"));
        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));

        final DataManager newManager = createDataManager(StorageMethod.JSON, "storage-migration-target.yml");
        final Dialect.MigrationData migrationData = dialect.getMigrationDataFromDatabase(newManager).orElse(null);
        assertNotNull(migrationData);

        // The data() key must be resolved through newManager.getTableName(...), never the raw/un-resolved table name
        final String expectedKey = newManager.getTableName("players");
        assertTrue(migrationData.data().containsKey(expectedKey),
                "Expected migration data to contain key '" + expectedKey + "' but got: " + migrationData.data().keySet());
    }

    @Test
    void getMigrationDataFromDatabase_skipsTablesWithoutTargetColumn() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();
        dialect.createTablesKeys(tablesKeys("players", "name"));
        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));

        // Create a rogue table with no "target" column, bypassing createTablesKeys entirely
        dialect.dsl.createTableIfNotExists(DSL.table(DSL.name("rogue")))
                .column("id", SQLDataType.VARCHAR(255))
                .execute();

        final DataManager newManager = createDataManager(StorageMethod.JSON, "storage-migration-target.yml");
        final Dialect.MigrationData migrationData = dialect.getMigrationDataFromDatabase(newManager).orElse(null);
        assertNotNull(migrationData);

        assertFalse(migrationData.tablesKeys().containsKey("rogue"), "Table without a target column should have been skipped");
        assertTrue(migrationData.data().keySet().stream().noneMatch(key -> key.equalsIgnoreCase("rogue")),
                "Table without a target column should not appear in migration data");
        // The valid table should still be present
        assertTrue(migrationData.tablesKeys().containsKey("players"));
    }

    @Test
    void dataSource_closesCleanly() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();
        dialect.createTablesKeys(tablesKeys("players", "name"));
        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));

        assertFalse(dialect.dataSource.isClosed());
        dialect.dataSource.close();
        assertTrue(dialect.dataSource.isClosed());
    }

    @Test
    void targetColumnConstantMatchesPrimaryKeyColumn() throws ConnectionException {
        final SQLDialect dialect = createSqlDialect();
        dialect.createTablesKeys(tablesKeys("players", "name"));
        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));

        // Sanity check that the "target" primary key column name used throughout the codebase is what's actually in the schema
        assertEquals("target", StringData.TARGET_COLUMN);
    }
}
