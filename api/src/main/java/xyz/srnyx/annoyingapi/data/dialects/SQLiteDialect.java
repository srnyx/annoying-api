package xyz.srnyx.annoyingapi.data.dialects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.data.DataManager;
import xyz.srnyx.annoyingapi.data.StringData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;


/**
 * SQL dialect for SQLite
 */
public class SQLiteDialect implements SQLDialect {
    @NotNull private final DataManager dataManager;

    /**
     * Construct a new {@link SQLiteDialect} with the given {@link DataManager}
     *
     * @param   dataManager the {@link DataManager} to use
     */
    public SQLiteDialect(@NotNull DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override @NotNull
    public String createTable(@NotNull String table) {
        return "CREATE TABLE IF NOT EXISTS \"" + table + "\" (\"" + StringData.TARGET_COLUMN + "\" TEXT PRIMARY KEY)";
    }

    @Override @Nullable
    public String createColumn(@NotNull String table, @NotNull String column) {
        try (final ResultSet result = dataManager.connection.createStatement().executeQuery("PRAGMA table_info(\"" + table + "\")")) {
            if (result != null) while (result.next()) if (result.getString("name").equals(column)) return null;
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to get table info for " + table, e);
        }
        return "ALTER TABLE \"" + table + "\" ADD COLUMN \"" + column + "\" TEXT";
    }

    @Override @NotNull
    public PreparedStatement getValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("SELECT \"" + column + "\" FROM \"" + table + "\" WHERE " + StringData.TARGET_COLUMN + " = ?");
        statement.setString(1, target);
        return statement;
    }

    @Override @NotNull
    public PreparedStatement setValue(@NotNull String table, @NotNull String target, @NotNull String column, @NotNull String value) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("INSERT OR REPLACE INTO `" + table + "` (`" + StringData.TARGET_COLUMN + "`, `" + column + "`) VALUES(?, ?)");
        statement.setString(1, target);
        statement.setString(2, value);
        return statement;
    }

    @Override @NotNull
    public PreparedStatement removeValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("UPDATE `" + table + "` SET `" + column + "` = NULL WHERE " + StringData.TARGET_COLUMN + " = ?");
        statement.setString(1, target);
        return statement;
    }
}
