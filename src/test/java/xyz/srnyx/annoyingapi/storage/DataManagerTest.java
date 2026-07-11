package xyz.srnyx.annoyingapi.storage;

import org.junit.jupiter.api.Test;
import xyz.srnyx.annoyingapi.storage.dialects.SQLDialect;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class DataManagerTest extends StorageTestSupport {

    @Test
    void getTableName_localMethod_hasNoPrefixButIsLowercased() throws ConnectionException {
        final DataManager manager = createDataManager(StorageMethod.H2);
        assertEquals("players", manager.getTableName("Players"));
        assertEquals("", manager.tablePrefix);
    }

    @Test
    void remoteConnection_defaultTablePrefix_derivedFromPluginName() {
        // Doesn't require a live connection - just the static default-prefix logic used by remote methods
        final String prefix = StorageConfig.RemoteConnection.getDefaultTablePrefix(PLUGIN);
        assertTrue(prefix.endsWith("_"));
        assertEquals(PLUGIN.getName().toLowerCase().replaceAll("[^a-z0-9]", "") + "_", prefix);
    }

    @Test
    void attemptDatabaseMigration_migratesDataAndSwapsFiles() throws ConnectionException {
        // Old manager: JSON, with some data written
        final DataManager oldManager = createDataManager(StorageMethod.JSON, "storage.yml");
        assertNull(oldManager.dialect.setToDatabase("players", "player1", "name", "Alice"));

        // Prepare storage-new.yml on disk, pointing at H2
        final StorageConfig newConfig = PLUGIN.newStorageConfig("storage-new.yml");
        assertNotNull(newConfig);
        newConfig.method = StorageMethod.H2;
        newConfig.save();

        // Run the migration
        final DataManager migratedManager = oldManager.attemptDatabaseMigration();
        assertInstanceOf(SQLDialect.class, migratedManager.dialect);
        assertEquals(StorageMethod.H2, migratedManager.storageConfig.method);
        assertEquals("Alice", migratedManager.dialect.getFromDatabase(migratedManager.getTableName("players"), "player1", "name").orElse(null));

        // File swap: storage.yml -> storage-old.yml, storage-new.yml -> storage.yml
        final File dataFolder = PLUGIN.getDataFolder();
        assertTrue(new File(dataFolder, "storage-old.yml").exists(), "storage-old.yml should exist after migration");
        assertTrue(new File(dataFolder, "storage.yml").exists(), "storage.yml should exist after migration");
        assertFalse(new File(dataFolder, "storage-new.yml").exists(), "storage-new.yml should have been renamed away");

        ((SQLDialect) migratedManager.dialect).dataSource.close();
    }

    @Test
    void attemptDatabaseMigration_resolvesMixedCaseTableNameThroughFullPipeline() throws ConnectionException {
        // Old manager: JSON, with data written under a mixed-case table name
        final DataManager oldManager = createDataManager(StorageMethod.JSON, "storage.yml");
        assertNull(oldManager.dialect.setToDatabase("Players", "player1", "name", "Alice"));

        // Prepare storage-new.yml on disk, pointing at H2
        final StorageConfig newConfig = PLUGIN.newStorageConfig("storage-new.yml");
        assertNotNull(newConfig);
        newConfig.method = StorageMethod.H2;
        newConfig.save();

        // Run the migration through the full production pipeline (not just Dialect-level extraction)
        final DataManager migratedManager = oldManager.attemptDatabaseMigration();
        assertInstanceOf(SQLDialect.class, migratedManager.dialect);

        // The migrated data must be reachable under the resolved (lowercased) table name
        assertEquals("Alice", migratedManager.dialect.getFromDatabase(migratedManager.getTableName("Players"), "player1", "name").orElse(null));

        ((SQLDialect) migratedManager.dialect).dataSource.close();
    }
}
