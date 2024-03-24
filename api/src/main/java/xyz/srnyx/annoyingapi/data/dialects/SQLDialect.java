package xyz.srnyx.annoyingapi.data.dialects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * SQL dialect for a specific type of database
 */
public interface SQLDialect {
    /**
     * Create a new table in the database with the {@code target} primary key column
     *
     * @param   table   the name of the table to create
     *
     * @return          the SQL query to create the table
     */
    @NotNull
    String createTable(@NotNull String table);

    /**
     * Create a new column in a table
     *
     * @param   table   the name of the table
     * @param   column  the name of the column to create
     *
     * @return          the SQL query to create the column
     */
    @Nullable
    String createColumn(@NotNull String table, @NotNull String column);

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
    PreparedStatement getValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException;

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
    PreparedStatement setValue(@NotNull String table, @NotNull String target, @NotNull String column, @NotNull String value) throws SQLException;

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
    PreparedStatement removeValue(@NotNull String table, @NotNull String target, @NotNull String column) throws SQLException;
}
