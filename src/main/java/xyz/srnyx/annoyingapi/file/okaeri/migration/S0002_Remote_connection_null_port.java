package xyz.srnyx.annoyingapi.file.okaeri.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;
import xyz.srnyx.annoyingapi.storage.StorageMethod;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.*;


public class S0002_Remote_connection_null_port extends NamedMigration {
    public S0002_Remote_connection_null_port() {
        super("Sets remote_connection.port if null",
                when(
                        not(exists("remote_connection.port")),
                        (config, view) -> {
                            final StorageMethod method = view.getOr("remote_connection.method", StorageMethod.class, StorageMethod.H2);
                            view.set("remote_connection.port", method.sqlInfo != null && method.sqlInfo.defaultPort() != null ? method.sqlInfo.defaultPort() : 3306);
                            return true;
                        }));
    }
}
