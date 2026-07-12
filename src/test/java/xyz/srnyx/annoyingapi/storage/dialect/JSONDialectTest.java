package xyz.srnyx.annoyingapi.storage.dialect;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import xyz.srnyx.annoyingapi.storage.*;
import xyz.srnyx.annoyingapi.storage.dialects.Dialect;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link xyz.srnyx.annoyingapi.storage.dialects.JSONDialect} (no database involved, plain file I/O)
 */
class JSONDialectTest extends StorageTestSupport {

    @NotNull
    private Dialect createJsonDialect() throws ConnectionException {
        return createDataManager(StorageMethod.JSON).dialect;
    }

    @Test
    void setAndGet_roundTrips() throws ConnectionException {
        final Dialect dialect = createJsonDialect();

        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));
        assertEquals("Alice", dialect.getFromDatabase("players", "player1", "name").orElse(null));
    }

    @Test
    void getFromDatabase_doesNotCrossContaminateBetweenTargets() throws ConnectionException {
        final Dialect dialect = createJsonDialect();

        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));
        assertNull(dialect.setToDatabase("players", "player2", "name", "Bob"));

        assertEquals("Alice", dialect.getFromDatabase("players", "player1", "name").orElse(null));
        assertEquals("Bob", dialect.getFromDatabase("players", "player2", "name").orElse(null));
        assertTrue(dialect.getFromDatabase("players", "player3", "name").isEmpty());
    }

    @Test
    void setToDatabase_multiKeyMapUpsertIsAllReadableAfterward() throws ConnectionException {
        final Dialect dialect = createJsonDialect();

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
        final Dialect dialect = createJsonDialect();

        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));
        assertTrue(dialect.removeValueFromDatabase("players", "player1", "name"));

        assertTrue(dialect.getFromDatabase("players", "player1", "name").isEmpty());
    }

    @Test
    void getMigrationDataFromDatabase_returnsWhatWasWritten() throws ConnectionException {
        final Dialect dialect = createJsonDialect();
        assertNull(dialect.setToDatabase("players", "player1", "name", "Alice"));
        assertNull(dialect.setToDatabase("players", "player2", "name", "Bob"));

        final DataManager newManager = createDataManager(StorageMethod.H2, "storage-migration-target.yml");
        final Dialect.MigrationData migrationData = dialect.getMigrationDataFromDatabase(newManager).orElse(null);
        assertNotNull(migrationData);

        assertTrue(migrationData.tablesKeys().containsKey("players"));
        assertTrue(migrationData.tablesKeys().get("players").contains("name"));

        final String newTableName = newManager.getTableName("players");
        final ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>> playersData = migrationData.data().get(newTableName);
        assertNotNull(playersData);
        assertEquals("Alice", playersData.get("player1").get("name").value());
        assertEquals("Bob", playersData.get("player2").get("name").value());
    }

    @Test
    void getMigrationDataFromDatabase_resolvesTableNameThroughNewManager() throws ConnectionException {
        final Dialect dialect = createJsonDialect();
        // Dialect.setToDatabase normalizes (lowercases) the table name before it's ever written, so "Players" is
        // stored as "players" on disk - this only confirms migration data keys go through newManager.getTableName(...)
        // rather than being copied verbatim, not that mixed case survives to the read side
        assertNull(dialect.setToDatabase("Players", "player1", "name", "Alice"));

        final DataManager newManager = createDataManager(StorageMethod.H2, "storage-migration-target.yml");
        final Dialect.MigrationData migrationData = dialect.getMigrationDataFromDatabase(newManager).orElse(null);
        assertNotNull(migrationData);

        final String expectedKey = newManager.getTableName("players");
        assertTrue(
                migrationData.data().containsKey(expectedKey),
                "Expected migration data to contain key '" + expectedKey + "' but got: " + migrationData.data().keySet());
    }
}
