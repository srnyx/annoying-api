package xyz.srnyx.annoyingapi.storage.dialects.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.storage.ConnectionException;
import xyz.srnyx.annoyingapi.storage.DataManager;
import xyz.srnyx.annoyingapi.data.StringData;
import xyz.srnyx.annoyingapi.storage.FailedSet;
import xyz.srnyx.annoyingapi.storage.Value;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


/**
 * SQL dialect for SQLite
 */
public class SQLiteDialect extends SQLDialect {
    /**
     * Creates a new SQLite dialect
     *
     * @param   dataManager {@link #dataManager}
     *
     * @throws  ConnectionException if a database connection error occurs
     */
    public SQLiteDialect(@NotNull DataManager dataManager) throws ConnectionException {
        super(dataManager);
    }

    @Override @NotNull
    public PreparedStatement getTablesImpl() throws SQLException {
        return connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table'");
    }

    @Override @NotNull
    public PreparedStatement createTableImpl(@NotNull String table) throws SQLException {
        return connection.prepareStatement("CREATE TABLE IF NOT EXISTS \"" + table + "\" (\"" + StringData.TARGET_COLUMN + "\" TEXT PRIMARY KEY)");
    }

    @Override @Nullable
    public PreparedStatement createKeyImpl(@NotNull String table, @NotNull String key) throws SQLException {
        try (final ResultSet result = connection.createStatement().executeQuery("PRAGMA table_info(\"" + table + "\")")) {
            if (result != null) while (result.next()) if (result.getString("name").equals(key)) return null;
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "&cFailed to get table info for &4" + table, e);
        }
        return connection.prepareStatement("ALTER TABLE \"" + table + "\" ADD COLUMN \"" + key + "\" TEXT");
    }

    @Override @NotNull
    protected PreparedStatement getAllValuesFromDatabaseImpl(@NotNull String table) throws SQLException {
        return connection.prepareStatement("SELECT * FROM \"" + table + "\"");
    }

    @Override @NotNull
    public Optional<String> getFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String column) {
        try (final PreparedStatement statement = connection.prepareStatement("SELECT \"" + column + "\" FROM \"" + table + "\" WHERE " + StringData.TARGET_COLUMN + " = ?")) {
            statement.setString(1, target);
            final ResultSet result = statement.executeQuery();
            if (result.next()) return Optional.ofNullable(result.getString(column));
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "&cFailed to get &4" + column + "&c for &4" + target + "&c in &4" + table + "&c | DEVELOPERS: Make sure you added the table/key to DataOptions!", e);
        }
        return Optional.empty();
    }

    @Override @Nullable
    public FailedSet setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String column, @NotNull String value) {
        try (final PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO `" + table + "` (`" + StringData.TARGET_COLUMN + "`, `" + column + "`) VALUES(?, ?)")) {
            statement.setString(1, target);
            statement.setString(2, value);
            statement.executeUpdate();
            return null;
        } catch (final SQLException e) {
            return new FailedSet(table, target, column, value, e);
        }
    }

    @Override @NotNull
    public Set<FailedSet> setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull ConcurrentHashMap<String, Value> data) {
        final Set<ConcurrentHashMap.Entry<String, Value>> entrySet = data.entrySet();

        // Get builders
        final StringBuilder insertBuilder = new StringBuilder("INSERT OR REPLACE INTO \"" + table + "\" (\"" + StringData.TARGET_COLUMN + "\"");
        final StringBuilder valuesBuilder = new StringBuilder(" VALUES(?");
        final List<Value> values = new ArrayList<>();
        for (final ConcurrentHashMap.Entry<String, Value> entry : entrySet) {
            insertBuilder.append(", `").append(entry.getKey()).append("`");
            valuesBuilder.append(", ?");
            values.add(entry.getValue());
        }
        insertBuilder.append(")");
        valuesBuilder.append(")");

        // Create statement
        final Set<FailedSet> failed = new HashSet<>();
        try (final PreparedStatement statement = setValuesParameters(target, values, insertBuilder, valuesBuilder, null)) {
            statement.executeUpdate();
        } catch (final SQLException e) {
            for (final ConcurrentHashMap.Entry<String, Value> entry : entrySet) failed.add(new FailedSet(table, target, entry.getKey(), entry.getValue().value, e));
        }
        return failed;
    }

    @Override
    public boolean removeFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String column) {
        try (final PreparedStatement statement = connection.prepareStatement("UPDATE `" + table + "` SET `" + column + "` = NULL WHERE " + StringData.TARGET_COLUMN + " = ?")) {
            statement.setString(1, target);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            return false;
        }
    }
}
