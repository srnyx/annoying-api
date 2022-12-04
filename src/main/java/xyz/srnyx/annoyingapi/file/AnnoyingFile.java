package xyz.srnyx.annoyingapi.file;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * Represents a file in the plugin's folder
 */
public interface AnnoyingFile {
    /**
     * Every {@link AnnoyingFile} should extend {@link YamlConfiguration}
     *
     * @return  the {@link YamlConfiguration} for this file
     */
    @NotNull
    YamlConfiguration getYaml();

    /**
     * The path is from the plugin's folder, not the server/root folder (unless otherwise specified)
     *
     * @return  the path to this file
     */
    @NotNull
    String getPath();

    /**
     * The file is constructed using {@link #getPath()}
     *
     * @return  the {@link File}
     */
    @NotNull
    File getFile();

    /**
     * This {@code boolean} is used by {@link #save()}
     *
     * @return  whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save()} is used
     */
    boolean canBeEmpty();

    /**
     * Loads the {@link #getYaml()} from the path
     */
    default void load() {
        final File file = getFile();
        if (!file.exists()) create();
        try {
            getYaml().load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the {@link #getFile()}
     */
    void create();

    /**
     * Deletes the {@link #getFile()}
     */
    default void delete() {
        try {
            Files.delete(getFile().toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets a value in the {@link #getYaml()}
     *
     * @param   path    the path to the value
     * @param   value   the value to set
     * @param   save    whether to save the file after setting the value
     */
    default void set(@NotNull String path, @Nullable Object value, boolean save) {
        getYaml().set(path, value);
        if (save) save();
    }

    /**
     * Saves the {@link #getYaml()} to the {@link #getFile()}
     */
    default void save() {
        final File file = getFile();
        final YamlConfiguration yaml = getYaml();

        // Delete file if it can't be empty and file is empty
        if (!canBeEmpty() && file.exists() && yaml.getKeys(true).isEmpty()) {
            delete();
            return;
        }

        // Create file if it can be empty and doesn't exist
        if (canBeEmpty() && !file.exists()) create();

        // Save file
        try {
            yaml.save(file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
