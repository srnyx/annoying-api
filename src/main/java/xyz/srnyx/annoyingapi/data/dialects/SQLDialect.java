package xyz.srnyx.annoyingapi.data.dialects;

import com.google.common.collect.ImmutableSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.data.DataManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;


/**
 * SQL dialect for a specific type of database
 */
public abstract class SQLDialect {
    /**
     * The {@link DataManager} to use for database operations
     */
    @NotNull protected final DataManager dataManager;

    /**
     * Construct a new {@link SQLDialect} with the given {@link DataManager}
     *
     * @param   dataManager {@link #dataManager}
     */
    public SQLDialect(@NotNull DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Get the tables in the database
     *
     * @return                  the prepared statement to get the tables
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    public PreparedStatement getTables() throws SQLException {
        return dataManager.connection.prepareStatement("SHOW TABLES");
    }

    /**
     * Create a new table in the database with the {@code target} primary key column
     *
     * @param   table           the name of the table to create
     *
     * @return                  the {@link PreparedStatement} to create the table
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    public PreparedStatement createTable(@NotNull String table) throws SQLException {
        return createTableImpl(table);
    }

    /**
     * Create a new column in a table
     *
     * @param   table           the name of the table
     * @param   column          the name of the column to create
     *
     * @return                  the prepared statement to create the column
     *
     * @throws  SQLException    if a database access error occurs
     */
    @Nullable
    public PreparedStatement createColumn(@NotNull String table, @NotNull String column) throws SQLException {
        return createColumnImpl(table, column.toLowerCase());
    }

    /**
     * Get all the values in a table
     *
     * @param   table           the name of the table
     *
     * @return                  the prepared statement to get the values
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    public PreparedStatement getValues(@NotNull String table) throws SQLException {
        return getValuesImpl(table);
    }

    /**
     * Get the value of a column in a table with respect to the {@code target}
     *
     * @param   table           the name of the table
     * @param   target          the target to get the value from
     * @param   column          the column to get the value from
     *
     * @return                  the prepared statement to get the value
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    public PreparedStatement getValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        return getValueImpl(table, target, column.toLowerCase());
    }

    /**
     * Set the value of a column in a table with respect to the {@code target}
     *
     * @param   table           the name of the table
     * @param   target          the target to set the value to
     * @param   column          the column to set the value to
     * @param   value           the value to set
     *
     * @return                  the prepared statement to set the value
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    public PreparedStatement setValue(@NotNull String table, @NotNull String target, @NotNull String column, @NotNull String value) throws SQLException {
        return setValueImpl(table, target, column.toLowerCase(), value);
    }

    /**
     * Set the values of columns in a table with respect to the {@code target}
     *
     * @param   table           the name of the table
     * @param   target          the target to set the values to
     * @param   data            the data to set (column, value)
     *
     * @return                  the prepared statement to set the values
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    public PreparedStatement setValues(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data) throws SQLException {
        return setValuesImpl(table, target, data.entrySet()
                .stream()
                .collect(HashMap::new, (m, e) -> m.put(e.getKey().toLowerCase(), e.getValue()), HashMap::putAll));
    }

    /**
     * Set the values of columns in a table with respect to the {@code target}
     *
     * @param   data    the data to set (table, (target, (column, value)))
     *
     * @return          the prepared statements to set the values
     */
    @NotNull
    public ImmutableSet<SetValueStatement> setValues(@NotNull Map<String, Map<String, Map<String, String>>> data) {
        final Set<SetValueStatement> statements = new HashSet<>();
        for (final Map.Entry<String, Map<String, Map<String, String>>> entry : data.entrySet()) {
            final String table = entry.getKey();
            for (final Map.Entry<String, Map<String, String>> entry1 : entry.getValue().entrySet()) {
                final String target = entry1.getKey();
                final Map<String, String> values = entry1.getValue();
                try {
                    statements.add(new SetValueStatement(table, target, values));
                } catch (final SQLException e) {
                    AnnoyingPlugin.log(Level.SEVERE, "&cFailed to set values for &4" + target + "&c in table &4" + table + "&c: &4" + values, e);
                }
            }
        }
        return ImmutableSet.copyOf(statements);
    }

    /**
     * Remove the value of a column in a table with respect to the {@code target}
     *
     * @param   table           the name of the table
     * @param   target          the target to remove the value from
     * @param   column          the column to remove the value from
     *
     * @return                  the prepared statement to remove the value
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    public PreparedStatement removeValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException {
        return removeValueImpl(table, target, column.toLowerCase());
    }

    /**
     * Create a new table in the database with the {@code target} primary key column
     *
     * @param   table           the name of the table to create
     *
     * @return                  the {@link PreparedStatement} to create the table
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    protected abstract PreparedStatement createTableImpl(@NotNull String table) throws SQLException;

    /**
     * Create a new column in a table
     *
     * @param   table           the name of the table
     * @param   column          the name of the column to create
     *
     * @return                  the prepared statement to create the column
     *
     * @throws  SQLException    if a database access error occurs
     */
    @Nullable
    protected abstract PreparedStatement createColumnImpl(@NotNull String table, @NotNull String column) throws SQLException;

    /**
     * Get all the values in a table
     *
     * @param   table           the name of the table
     *
     * @return                  the prepared statement to get the values
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    protected abstract PreparedStatement getValuesImpl(@NotNull String table) throws SQLException;

    /**
     * Get the value of a column in a table with respect to the {@code target}
     *
     * @param   table           the name of the table
     * @param   target          the target to get the value from
     * @param   column          the column to get the value from
     *
     * @return                  the prepared statement to get the value
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    protected abstract PreparedStatement getValueImpl(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException;

    /**
     * Set the value of a column in a table with respect to the {@code target}
     *
     * @param   table           the name of the table
     * @param   target          the target to set the value to
     * @param   column          the column to set the value to
     * @param   value           the value to set
     *
     * @return                  the prepared statement to set the value
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    protected abstract PreparedStatement setValueImpl(@NotNull String table, @NotNull String target, @NotNull String column, @NotNull String value) throws SQLException;

    /**
     * Set the values of columns in a table with respect to the {@code target}
     *
     * @param   table           the name of the table
     * @param   target          the target to set the values to
     * @param   data            the data to set (column, value)
     *
     * @return                  the prepared statement to set the values
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    protected abstract PreparedStatement setValuesImpl(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data) throws SQLException;

    /**
     * Used for {@link #setValueImpl(String, String, String, String)} to set the parameters of the {@link PreparedStatement}
     *
     * @param   target          the target to set the value to
     * @param   values          the values to set
     * @param   insertBuilder   the insert query builder
     * @param   valuesBuilder   the values query builder
     * @param   updateBuilder   the update query builder
     *
     * @return                  the prepared statement with the set parameters
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    protected PreparedStatement setValuesParameters(@NotNull String target, @NotNull List<String> values, @NotNull StringBuilder insertBuilder, @NotNull StringBuilder valuesBuilder, @Nullable StringBuilder updateBuilder) throws SQLException {
        final boolean hasUpdate = updateBuilder != null;

        // Get query
        final StringBuilder query = insertBuilder.append(valuesBuilder);
        if (hasUpdate) query.append(updateBuilder);

        // Create statement & set parameters
        final PreparedStatement statement = dataManager.connection.prepareStatement(query.toString());
        statement.setString(1, target);
        int i = 2;
        for (final String value : values) statement.setString(i++, value);
        if (hasUpdate) for (final String value : values) statement.setString(i++, value);

        return statement;
    }

    /**
     * Remove the value of a column in a table with respect to the {@code target}
     *
     * @param   table           the name of the table
     * @param   target          the target to remove the value from
     * @param   column          the column to remove the value from
     *
     * @return                  the prepared statement to remove the value
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    protected abstract PreparedStatement removeValueImpl(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException;

    /**
     * Holds information about a {@link #setValues(String, String, Map) set values} statement
     */
    public class SetValueStatement {
        /**
         * The name of the table
         */
        @NotNull public final String table;
        /**
         * The target to set the values to
         */
        @NotNull public final String target;
        /**
         * The values to set
         */
        @NotNull public final Map<String, String> values;
        /**
         * The {@link PreparedStatement} to use for setting the values
         */
        @NotNull public final PreparedStatement statement;

        /**
         * Construct a new {@link SetValueStatement} with the given parameters
         *
         * @param   table           {@link #table}
         * @param   target          {@link #target}
         * @param   values          {@link #values}
         *
         * @throws  SQLException    if a database access error occurs
         */
        public SetValueStatement(@NotNull String table, @NotNull String target, @NotNull Map<String, String> values) throws SQLException {
            this.table = table;
            this.target = target;
            this.values = values;
            this.statement = setValues(table, target, values);
        }
    }
}
