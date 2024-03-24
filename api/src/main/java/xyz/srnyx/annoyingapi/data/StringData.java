package xyz.srnyx.annoyingapi.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;


/**
 * A data class for storing and retrieving string data from SQL databases
 */
public class StringData extends Data<String> {
    /**
     * The primary target column name
     */
    @NotNull public static final String TARGET_COLUMN = "target";

    /**
     * The {@link DataManager data manager} instance
     */
    @NotNull private final DataManager dataManager;
    /**
     * The name of the table in the database to get/store the data from/to
     */
    @NotNull private final String table;

    /**
     * Construct a new {@link StringData} for the given string
     *
     * @param   plugin  {@link #plugin}
     * @param   table   {@link #table}
     * @param   string  {@link #target}
     */
    public StringData(@NotNull AnnoyingPlugin plugin, @NotNull String table, @NotNull String string) {
        super(plugin, string, string);
        if (plugin.dataManager == null) throw new IllegalStateException(plugin.options.dataOptions.enabled ? "Data manager is not initialized!" : "Data manager is not enabled! Plugin devs: enable it by setting options.dataOptions.enabled to true");
        this.dataManager = plugin.dataManager;
        this.table = dataManager.getTableName(table);
    }

    @Override @Nullable
    public String get(@NotNull String key) {
        try {
            final ResultSet result = dataManager.dialect.getValue(table, target, key).executeQuery();
            String string = null;
            if (result.next()) string = result.getString(1);
            result.close();
            return string;
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to get " + key + " for " + target + " in " + table + ". Make sure you added the table/column to DataOptions!", e);
        }
        return null;
    }

    @Override
    protected boolean set(@NotNull String key, @NotNull String value) {
        try (final PreparedStatement statement = dataManager.dialect.setValue(table, target, key, value)) {
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to set " + key + " for " + target + " in " + table + ". Make sure you added the table/column to DataOptions!", e);
            return false;
        }
    }

    @Override
    public boolean remove(@NotNull String key) {
        try (final PreparedStatement statement = dataManager.dialect.removeValue(table, target, key)) {
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to remove " + key + " for " + target + " in " + table + ". Make sure you added the table/column to DataOptions!", e);
            return false;
        }
    }
}
