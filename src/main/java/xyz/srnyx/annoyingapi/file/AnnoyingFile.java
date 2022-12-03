package xyz.srnyx.annoyingapi.file;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;


/**
 * Represents a file in the plugin's folder
 */
public interface AnnoyingFile {
    /**
     * @return  the {@link YamlConfiguration} for this file
     */
    @NotNull
    YamlConfiguration getYaml();

    /**
     * @return  the path to this file
     */
    @NotNull
    String getPath();

    /**
     * @return  the {@link File}
     */
    @NotNull
    File getFile();

    /**
     * @return  whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save(AnnoyingPlugin)} is used
     */
    boolean canBeEmpty();

    /**
     * Loads the {@link #getYaml()} from the path
     *
     * @param   plugin  the {@link AnnoyingPlugin} instance
     */
    default void load(@Nullable AnnoyingPlugin plugin) {
        final File file = getFile();
        if (!file.exists()) create(plugin);
        try {
            getYaml().load(file);
        } catch (IOException | InvalidConfigurationException e) {
            warning("loading");
            e.printStackTrace();
        }
    }

    /**
     * Creates the {@link #getFile()}
     *
     * @param   plugin  the {@link AnnoyingPlugin} instance
     */
    void create(@Nullable AnnoyingPlugin plugin);

    /**
     * Deletes the {@link #getFile()}
     */
    default void delete() {
        try {
            Files.delete(getFile().toPath());
        } catch (Exception e) {
            warning("deleting");
            e.printStackTrace();
        }
    }

    /**
     * Sets a value in the {@link #getYaml()}
     *
     * @param   path    the path to the value
     * @param   value   the value to set
     * @param   plugin  the {@link AnnoyingPlugin} instance
     * @param   save    whether to save the file after setting the value
     */
    default void set(@NotNull String path, @Nullable Object value, @Nullable AnnoyingPlugin plugin, boolean save) {
        getYaml().set(path, value);
        if (save) save(plugin);
    }

    /**
     * Saves the {@link #getYaml()} to the {@link #getFile()}
     *
     * @param   plugin  the {@link AnnoyingPlugin} instance
     */
    default void save(@Nullable AnnoyingPlugin plugin) {
        final File file = getFile();
        final YamlConfiguration yaml = getYaml();

        // Delete file if it can't be empty and file is empty
        if (!canBeEmpty() && file.exists() && yaml.getKeys(true).isEmpty()) {
            delete();
            return;
        }

        // Create file if it can be empty and doesn't exist
        if (canBeEmpty() && !file.exists()) create(plugin);

        // Save file
        try {
            yaml.save(file);
        } catch (final IOException e) {
            warning("saving");
            e.printStackTrace();
        }
    }

    /**
     * Logs a warning to the console
     *
     * @param   action  the action that failed
     */
    default void warning(@NotNull String action) {
        AnnoyingPlugin.log(Level.WARNING, ChatColor.RED + "Error " + action + " " + ChatColor.DARK_RED + getPath() + ChatColor.RED + "!");
    }
}
