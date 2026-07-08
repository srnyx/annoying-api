package xyz.srnyx.annoyingapi.file.okaeri.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.storage.StorageConfig;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.*;


public class S0001_Remote_connection_null_table_prefix extends NamedMigration {
    public S0001_Remote_connection_null_table_prefix(@NotNull AnnoyingPlugin plugin) {
        super("Sets remote_connection.table_prefix if null", supply(
                "remote_connection.table_prefix",
                () -> StorageConfig.RemoteConnection.getDefaultTablePrefix(plugin)));
    }
}
