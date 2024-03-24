package xyz.srnyx.annoyingapi.data.dialects;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.data.DataManager;
import xyz.srnyx.annoyingapi.data.StringData;

import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * SQL dialect for H2 database
 */
public class H2Dialect implements SQLDialect {
    @NotNull private final DataManager dataManager;

    /**
     * Construct a new {@link H2Dialect} with the given {@link DataManager}
     *
     * @param   dataManager {@link #dataManager}
     */
    public H2Dialect(@NotNull DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override @NotNull
    public String createTable(@NotNull String table) {
        return "CREATE TABLE IF NOT EXISTS \"" + table + "\" (\"" + StringData.TARGET_COLUMN + "\" TEXT PRIMARY KEY)";
    }

    @Override @NotNull
    public String createColumn(@NotNull String table, @NotNull String column) {
        return "ALTER TABLE \"" + table + "\" ADD COLUMN IF NOT EXISTS \"" + column + "\" TEXT";
    }

    @Override @NotNull
    public PreparedStatement getValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("SELECT \"" + column + "\" FROM \"" + table + "\" WHERE \"" + StringData.TARGET_COLUMN + "\" = ?");
        statement.setString(1, target);
        return statement;
    }

    @Override @NotNull
    public PreparedStatement setValue(@NotNull String table, @NotNull String target, @NotNull String column, @NotNull String value) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("MERGE INTO \"" + table + "\" (\"" + StringData.TARGET_COLUMN + "\", \"" + column + "\") KEY(\"" + StringData.TARGET_COLUMN + "\") VALUES (?, ?)");
        statement.setString(1, target);
        statement.setString(2, value);
        return statement;
    }

    @Override @NotNull
    public PreparedStatement removeValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("UPDATE \"" + table + "\" SET \"" + column + "\" = NULL WHERE \"" + StringData.TARGET_COLUMN + "\" = ?");
        statement.setString(1, target);
        return statement;
    }
}
