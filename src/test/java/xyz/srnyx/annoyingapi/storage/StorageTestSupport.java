package xyz.srnyx.annoyingapi.storage;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.MockBukkitTestSupport;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Base class for storage/data-management tests that need a real {@link DataManager} backed by a given {@link StorageMethod}.
 */
public abstract class StorageTestSupport extends MockBukkitTestSupport {
    @NotNull
    protected DataManager createDataManager(@NotNull StorageMethod method) throws ConnectionException {
        return createDataManager(method, "storage.yml");
    }

    @NotNull
    protected DataManager createDataManager(@NotNull StorageMethod method, @NotNull String fileName) throws ConnectionException {
        final StorageConfig config = PLUGIN.newStorageConfig(fileName);
        assertNotNull(config, "Failed to build StorageConfig for " + fileName);
        config.method = method;
        return new DataManager(config);
    }
}
