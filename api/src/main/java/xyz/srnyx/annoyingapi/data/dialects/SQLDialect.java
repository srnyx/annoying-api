package xyz.srnyx.annoyingapi.data.dialects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.data.DataManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


/**
 * SQL dialect for a specific type of database
 */
public abstract class SQLDialect {
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
     * Create a new table in the database with the {@code target} primary key column
     *
     * @param   table   the name of the table to create
     *
     * @return          the SQL query to create the table
     */
    @NotNull
    public abstract String createTable(@NotNull String table);

    /**
     * Create a new column in a table
     *
     * @param   table   the name of the table
     * @param   column  the name of the column to create
     *
     * @return          the SQL query to create the column
     */
    @Nullable
    public abstract String createColumn(@NotNull String table, @NotNull String column);

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
    public abstract PreparedStatement getValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException;

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
    public abstract PreparedStatement setValue(@NotNull String table, @NotNull String target, @NotNull String column, @NotNull String value) throws SQLException;

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
    public abstract PreparedStatement setValues(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data) throws SQLException;

    @NotNull
    protected PreparedStatement setValuesParameters(@NotNull String target, @NotNull List<String> values, @NotNull String query) throws SQLException {
        final PreparedStatement statement = dataManager.connection.prepareStatement(query);
        statement.setString(1, target);
        int i = 2;
        for (final String value : values) statement.setString(i++, value);
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
    public abstract PreparedStatement removeValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException;
}
