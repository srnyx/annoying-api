package xyz.srnyx.annoyingapi.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import java.util.logging.Level;


/**
 * Represents a class that can be used to manage data
 *
 * @param   <T> the type of target to manage data for
 */
public abstract class Data<T> implements Annoyable {
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
    public Data(@NotNull AnnoyingPlugin plugin, @NotNull T target) {
        this.plugin = plugin;
        this.target = target;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    /**
     * Get the name of the target used for warning messages
     *
     * @return  the name of the target
     */
    @NotNull
    protected String getTargetName() {
        return target.getClass().getSimpleName();
    }

    /**
     * Check if the data value exists for the given key
     *
     * @param   key the key to check for
     *
     * @return      true if the data value exists, false otherwise
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
    @Nullable
    public String get(@NotNull String key, @Nullable String def) {
        final String value = get(key);
        return value == null ? def : value;
    }

    /**
     * Set the data value for the given key. If the key already exists, it will be overwritten
     *
     * @param   key     the key to set the data value for
     * @param   value   the data value to set, or null to remove the data value
     *
     * @return          this {@link Data} instance
     */
    @NotNull
    public Data<T> set(@NotNull String key, @Nullable Object value) {
        return value == null ? remove(key) : set(key, value.toString());
    }

    /**
     * Set the data value for the given key. If the key already exists, it will be overwritten
     *
     * @param   key     the key to set the data value for
     * @param   value   the data value to set
     *
     * @return          this {@link Data} instance
     */
    @NotNull
    protected abstract Data<T> set(@NotNull String key, @NotNull String value);

    /**
     * Remove the data value with the given key
     *
     * @param   key the key to remove the data value for
     *
     * @return      this {@link Data} instance
     */
    @NotNull
    public abstract Data<T> remove(@NotNull String key);

    /**
     * Send an error message to the console
     *
     * @param   action  the action that failed
     */
    protected void sendError(@NotNull String action) {
        AnnoyingPlugin.log(Level.WARNING, "&cFailed to " + action + " data for &4" + getTargetName() + "&c!");
    }
}
