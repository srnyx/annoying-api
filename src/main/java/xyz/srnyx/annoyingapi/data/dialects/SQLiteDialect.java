package xyz.srnyx.annoyingapi.data.dialects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.data.DataManager;
import xyz.srnyx.annoyingapi.data.StringData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


/**
 * SQL dialect for SQLite
 */
public class SQLiteDialect extends SQLDialect {
    /**
     * Creates a new SQLite dialect
     *
     * @param   dataManager {@link #dataManager}
     */
    public SQLiteDialect(@NotNull DataManager dataManager) {
        super(dataManager);
    }

    @Override @NotNull
    public PreparedStatement createTableImpl(@NotNull String table) throws SQLException {
        return dataManager.connection.prepareStatement("CREATE TABLE IF NOT EXISTS \"" + table + "\" (\"" + StringData.TARGET_COLUMN + "\" TEXT PRIMARY KEY)");
    }

    @Override @Nullable
    public PreparedStatement createColumnImpl(@NotNull String table, @NotNull String column) throws SQLException {
        try (final ResultSet result = dataManager.connection.createStatement().executeQuery("PRAGMA table_info(\"" + table + "\")")) {
            if (result != null) while (result.next()) if (result.getString("name").equals(column)) return null;
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to get table info for " + table, e);
        }
        return dataManager.connection.prepareStatement("ALTER TABLE \"" + table + "\" ADD COLUMN \"" + column + "\" TEXT");
    }

    @Override @NotNull
    protected PreparedStatement getValuesImpl(@NotNull String table) throws SQLException {
        return dataManager.connection.prepareStatement("SELECT * FROM \"" + table + "\"");
    }

    @Override @NotNull
    public PreparedStatement getValueImpl(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("SELECT \"" + column + "\" FROM \"" + table + "\" WHERE " + StringData.TARGET_COLUMN + " = ?");
        statement.setString(1, target);
        return statement;
    }

    @Override @NotNull
    public PreparedStatement setValueImpl(@NotNull String table, @NotNull String target, @NotNull String column, @NotNull String value) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("INSERT OR REPLACE INTO `" + table + "` (`" + StringData.TARGET_COLUMN + "`, `" + column + "`) VALUES(?, ?)");
        statement.setString(1, target);
        statement.setString(2, value);
        return statement;
    }

    @Override @NotNull
    public PreparedStatement setValuesImpl(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data) throws SQLException {
        // Get builders
        final StringBuilder insertBuilder = new StringBuilder("INSERT OR REPLACE INTO \"" + table + "\" (\"" + StringData.TARGET_COLUMN + "\"");
        final StringBuilder valuesBuilder = new StringBuilder(" VALUES(?");
        final List<String> values = new ArrayList<>();
        for (final Map.Entry<String, String> entry : data.entrySet()) {
            insertBuilder.append(", `").append(entry.getKey()).append("`");
            valuesBuilder.append(", ?");
            values.add(entry.getValue());
        }
        insertBuilder.append(")");
        valuesBuilder.append(")");

        // Create statement
        return setValuesParameters(target, values, insertBuilder.append(valuesBuilder).toString());
    }

    @Override @NotNull
    public PreparedStatement removeValueImpl(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("UPDATE `" + table + "` SET `" + column + "` = NULL WHERE " + StringData.TARGET_COLUMN + " = ?");
        statement.setString(1, target);
        return statement;
    }
}
