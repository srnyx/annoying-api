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
 * SQL dialect for MySQL database
 */
public class MySQLDialect extends SQLDialect {
    public MySQLDialect(@NotNull DataManager dataManager) {
        super(dataManager);
    }

    @Override @NotNull
    public String createTable(@NotNull String table) {
        return "CREATE TABLE IF NOT EXISTS `" + table + "` (`" + StringData.TARGET_COLUMN + "` VARCHAR(255) PRIMARY KEY)";
    }

    @Override @Nullable
    public String createColumn(@NotNull String table, @NotNull String column) {
        try (final ResultSet result = dataManager.connection.createStatement().executeQuery("SHOW COLUMNS FROM `" + table + "`")) {
            if (result != null) while (result.next()) if (result.getString("Field").equals(column)) return null;
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to get columns for " + table, e);
        }
        return "ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` TEXT";
    }

    @Override @NotNull
    public PreparedStatement getValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("SELECT `" + column + "` FROM `" + table + "` WHERE " + StringData.TARGET_COLUMN + " = ?");
        statement.setString(1, target);
        return statement;
    }

    @Override @NotNull
    public PreparedStatement setValue(@NotNull String table, @NotNull String target, @NotNull String column, @NotNull String value) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("INSERT INTO `" + table + "` (`" + StringData.TARGET_COLUMN + "`, `" + column + "`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `" + column + "` = ?");
        statement.setString(1, target);
        statement.setString(2, value);
        statement.setString(3, value);
        return statement;
    }

    @Override @NotNull
    public PreparedStatement setValues(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data) throws SQLException {
        // Get builders
        final StringBuilder insertBuilder = new StringBuilder("INSERT INTO `" + table + "` (`" + StringData.TARGET_COLUMN + "`");
        final StringBuilder valuesBuilder = new StringBuilder(" VALUES(?");
        final StringBuilder updateBuilder = new StringBuilder(" ON DUPLICATE KEY UPDATE ");
        final List<String> values = new ArrayList<>();
        for (final Map.Entry<String, String> entry : data.entrySet()) {
            final String column = entry.getKey();
            insertBuilder.append(", `").append(column).append("`");
            valuesBuilder.append(", ?");
            updateBuilder.append("`").append(column).append("` = ?, ");
            values.add(entry.getValue());
        }
        insertBuilder.append(")");
        valuesBuilder.append(")");
        updateBuilder.setLength(updateBuilder.length() - 2);

        // Create statement
        return setValuesParameters(target, values, insertBuilder.append(valuesBuilder).append(updateBuilder).toString());
    }

    @Override @NotNull
    public PreparedStatement removeValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement("UPDATE `" + table + "` SET `" + column + "` = NULL WHERE " + StringData.TARGET_COLUMN + " = ?");
        statement.setString(1, target);
        return statement;
    }
}
