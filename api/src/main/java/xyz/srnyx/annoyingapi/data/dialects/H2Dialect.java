package xyz.srnyx.annoyingapi.data.dialects;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.data.DataManager;
import xyz.srnyx.annoyingapi.data.StringData;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * SQL dialect for H2 database
 */
public class H2Dialect extends SQLDialect {
    /**
     * Creates a new H2 dialect
     *
     * @param   dataManager {@link #dataManager}
     */
    public H2Dialect(@NotNull DataManager dataManager) {
        super(dataManager);
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
        final PreparedStatement statement = dataManager.connection.prepareStatement("MERGE INTO \"" + table + "\" (\"" + StringData.TARGET_COLUMN + "\", \"" + column + "\") KEY(\"" + StringData.TARGET_COLUMN + "\") VALUES(?, ?)");
        statement.setString(1, target);
        statement.setString(2, value);
        return statement;
    }

    @Override @NotNull
    public PreparedStatement setValues(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data) throws SQLException {
        // Get builders
        final StringBuilder insertBuilder = new StringBuilder("MERGE INTO \"" + table + "\" (\"" + StringData.TARGET_COLUMN + "\"");
        final StringBuilder valuesBuilder = new StringBuilder(" VALUES(?");
        final List<String> values = new ArrayList<>();
        for (final Map.Entry<String, String> entry : data.entrySet()) {
            insertBuilder.append(", \"").append(entry.getKey()).append("\"");
            valuesBuilder.append(", ?");
            values.add(entry.getValue());
        }
        insertBuilder.append(") KEY(\"").append(StringData.TARGET_COLUMN).append("\")");
        valuesBuilder.append(")");

        // Create statement
        return setValuesParameters(target, values, insertBuilder.append(valuesBuilder).toString());
    }

    @Override @NotNull
    public PreparedStatement removeValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("UPDATE \"" + table + "\" SET \"" + column + "\" = NULL WHERE \"" + StringData.TARGET_COLUMN + "\" = ?");
        statement.setString(1, target);
        return statement;
    }
}
