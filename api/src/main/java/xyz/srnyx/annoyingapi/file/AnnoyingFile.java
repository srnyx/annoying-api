package xyz.srnyx.annoyingapi.file;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * Represents a file in the plugin's folder
 */
public abstract class AnnoyingFile extends YamlConfiguration {
    /**
     * The {@link AnnoyingPlugin} instance
     */
    @NotNull public final AnnoyingPlugin plugin;
    /**
     * The path to the file
     */
    @NotNull public final String path;
    /**
     * The file constructed using the {@link #path}
     */
    @NotNull public final File file;
    /**
     * The {@link FileOptions} for the file
     */
    @NotNull protected final FileOptions fileOptions;

    /**
     * Constructs a new {@link AnnoyingFile}
     *
     * @param   plugin          {@link #plugin}
     * @param   path            {@link #path}
     * @param   fileOptions     {@link #fileOptions}
     */
    protected AnnoyingFile(@NotNull AnnoyingPlugin plugin, @NotNull String path, @Nullable FileOptions fileOptions) {
        this.plugin = plugin;
        this.path = path;
        this.file = new File(plugin.getDataFolder(), path);
        this.fileOptions = fileOptions == null ? new FileOptions() : fileOptions;
        load();
    }

    /**
     * Constructs a new {@link AnnoyingFile}
     *
     * @param   plugin          {@link #plugin}
     * @param   path            {@link #path}
     */
    protected AnnoyingFile(@NotNull AnnoyingPlugin plugin, @NotNull String path) {
        this(plugin, path, null);
    }

    /**
     * Creates the {@link #file}
     */
    public abstract void create();

    /**
     * Loads the YAML from the path
     */
    public void load() {
        // Create the file if it doesn't exist and can be empty
        final boolean doesntExist = !file.exists();
        if (fileOptions.canBeEmpty && doesntExist) {
            create();
        } else if (doesntExist) {
            return;
        }

        // Load
        try {
            this.load(file);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the {@link #file}
     */
    public void delete() {
        try {
            Files.delete(file.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets a value in the YAML and then {@link #save() saves} the file
     *
     * @param   key     the key to set
     * @param   value   the value to set the key to
     */
    public void setSave(@Nullable String key, @NotNull Object value) {
        set(key, value);
        save();
    }

    /**
     * Saves the YAML to the {@link #file}
     */
    public void save() {
        // Stop process if it's empty when it can't be
        if (!fileOptions.canBeEmpty && getKeys(true).isEmpty()) {
            // Delete file if it exists
            if (file.exists()) delete();
            return;
        }

        // Create file if it can be empty and doesn't exist
        if (fileOptions.canBeEmpty && !file.exists()) create();

        // Save file
        try {
            save(file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A class to hold the options for a file
     */
    public static class FileOptions {
        /**
         * Whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save()} is used
         */
        public boolean canBeEmpty = true;

        /**
         * Sets the {@link #canBeEmpty}
         *
         * @param   canBeEmpty  {@link #canBeEmpty}
         *
         * @return              the {@link FileOptions} instance
         */
        @NotNull
        public FileOptions canBeEmpty(boolean canBeEmpty) {
            this.canBeEmpty = canBeEmpty;
            return this;
        }
    }
}
