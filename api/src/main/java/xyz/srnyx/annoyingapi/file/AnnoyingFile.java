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
     * Whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save()} is used
     */
    public final boolean canBeEmpty;

    /**
     * Constructs a new {@link AnnoyingFile}
     *
     * @param   plugin          the {@link AnnoyingPlugin} that is managing the file
     * @param   path            the path to the file
     * @param   subFolderPath   the path to prepend to the file path when constructing the {@link #file}
     * @param   canBeEmpty      whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save()} is used
     */
    protected AnnoyingFile(@NotNull AnnoyingPlugin plugin, @NotNull String path, @NotNull String subFolderPath, boolean canBeEmpty) {
        this.plugin = plugin;
        this.path = path;
        this.file = new File(plugin.getDataFolder(), subFolderPath + path);
        this.canBeEmpty = canBeEmpty;
        load();
    }

    /**
     * Creates the {@link #file}
     */
    public abstract void create();

    /**
     * Loads the YAML from the path
     */
    public void load() {
        // Create the file if it doesn't exist and it can be empty
        if (canBeEmpty && !file.exists()) {
            create();
        } else if (!file.exists()) {
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
     * Sets a value in the YAML
     *
     * @param   path    the path to the value
     * @param   value   the value to set
     * @param   save    whether to save the file after setting the value
     */
    public void set(@NotNull String path, @Nullable Object value, boolean save) {
        this.set(path, value);
        if (save) save();
    }

    /**
     * Saves the YAML to the {@link #file}
     */
    public void save() {
        // Stop process if it's empty when it can't be
        if (!canBeEmpty && this.getKeys(true).isEmpty()) {
            // Delete file if it exists
            if (file.exists()) delete();
            return;
        }

        // Create file if it can be empty and doesn't exist
        if (canBeEmpty && !file.exists()) create();

        // Save file
        try {
            this.save(file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
