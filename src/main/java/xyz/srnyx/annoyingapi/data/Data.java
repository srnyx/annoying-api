package xyz.srnyx.annoyingapi.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Annoyable;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.Optional;
import java.util.logging.Level;


/**
 * Represents a class that can be used to manage data
 *
 * @param   <T> the type of target to manage data for
 */
public abstract class Data<T> extends Stringable implements Annoyable {
    /**
     * The {@link AnnoyingPlugin plugin} instance
     */
    @NotNull protected final AnnoyingPlugin plugin;
    /**
     * The target to manage data for
     */
    @NotNull public final T target;

    /**
     * Construct a new {@link Data} for the given target
     *
     * @param   plugin  {@link #plugin}
     * @param   target  {@link #target}
     */
    protected Data(@NotNull AnnoyingPlugin plugin, @NotNull T target) {
        this.plugin = plugin;
        this.target = target;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    /**
     * Check if the data value exists for the given key
     *
     * @param   key the key to check for
     *
     * @return      {@code true} if the data value exists, {@code false} otherwise
     */
    public boolean has(@NotNull String key) {
        return get(key) != null;
    }

    /**
     * Get the data value for the given key
     *
     * @param   key the key to get the data value for
     *
     * @return      the data value, or null if not found
     */
    @Nullable
    public abstract String get(@NotNull String key);

    /**
     * Get the data value for the given key, or the default value if not found
     *
     * @param   key the key to get the data value for
     * @param   def the default value to return if the data value is not found
     *
     * @return      the data value, or the default value if not found
     */
    @NotNull
    public String get(@NotNull String key, @NotNull String def) {
        final String value = get(key);
        return value == null ? def : value;
    }

    /**
     * Get the data value for the given key as an {@link Optional}
     *
     * @param   key the key to get the data value for
     *
     * @return      the data value as an {@link Optional}
     */
    @NotNull
    public Optional<String> getOptional(@NotNull String key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * Set the data value for the given key. If the key already exists, it will be overwritten
     *
     * @param   key     the key to set the data value for
     * @param   value   the data value to set
     *
     * @return          {@code true} if the data value was set successfully, {@code false} otherwise
     */
    protected abstract boolean set(@NotNull String key, @NotNull String value);

    /**
     * Set the data value for the given key. If the key already exists, it will be overwritten
     *
     * @param   key     the key to set the data value for
     * @param   value   the data value to set, or null to remove the data value
     *
     * @return          {@code true} if the data value was set successfully, {@code false} otherwise
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean set(@NotNull String key, @Nullable Object value) {
        return value == null ? remove(key) : set(key, value.toString());
    }

    /**
     * Set the data value for the given key. If the key already exists, it will be overwritten
     *
     * @param   key     the key to set the data value for
     * @param   value   the data value to set, or null to remove the data value
     *
     * @return          this {@link Data} instance for chaining
     */
    @NotNull
    public Data<T> setChain(@NotNull String key, @Nullable Object value) {
        set(key, value);
        return this;
    }

    /**
     * Remove the data value with the given key
     *
     * @param   key the key to remove the data value for
     *
     * @return      {@code true} if the data value was removed successfully, {@code false} otherwise
     */
    public abstract boolean remove(@NotNull String key);

    /**
     * Remove the data value with the given key
     *
     * @param   key the key to remove the data value for
     *
     * @return      this {@link Data} instance for chaining
     */
    @NotNull
    public Data<T> removeChain(@NotNull String key) {
        remove(key);
        return this;
    }

    /**
     * Send an error message to the console
     *
     * @param   action  the action that failed
     * @param   t       the {@link Throwable} that caused the error
     */
    protected void sendError(@NotNull String action, @Nullable Throwable t) {
        AnnoyingPlugin.log(Level.WARNING, "&cFailed to " + action + " data for &4" + BukkitUtility.stripUntranslatedColor(target.toString()) + "&c!", t);
    }
}
