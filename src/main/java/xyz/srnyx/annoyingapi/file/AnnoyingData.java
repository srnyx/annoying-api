package xyz.srnyx.annoyingapi.file;

import org.bukkit.configuration.file.YamlConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * Represents a file in the plugin's folder (in {@code /data/})
 */
public class AnnoyingData extends YamlConfiguration implements AnnoyingFile {
    @NotNull private final String path;
    @NotNull private final File file;
    private final boolean canBeEmpty;

    /**
     * Constructs and loads a new {@link AnnoyingData} from the path
     *
     * @param   path        the path to the file (relative to {@link AnnoyingPlugin#PLUGIN_FOLDER}{@code /data/})
     * @param   canBeEmpty  whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save(AnnoyingPlugin)} is used
     */
    public AnnoyingData(@NotNull String path, boolean canBeEmpty) {
        this.path = path;
        this.file = new File(new File(AnnoyingPlugin.PLUGIN_FOLDER, "data"), path);
        this.canBeEmpty = canBeEmpty;
        load();
    }

    /**
     * Constructs and loads a new {@link AnnoyingData} from the path
     *
     * @param   path    the path to the file (inside {@code /data/})
     */
    public AnnoyingData(@NotNull String path) {
        this(path, true);
    }

    @Override @NotNull
    public YamlConfiguration getYaml() {
        return this;
    }
    @Override @NotNull
    public String getPath() {
        return path;
    }
    @Override @NotNull
    public File getFile() {
        return file;
    }
    @Override
    public boolean canBeEmpty() {
        return canBeEmpty;
    }

    @Override
    public void create(@Nullable AnnoyingPlugin plugin) {
        try {
            Files.createFile(file.toPath());
        } catch (final IOException e) {
            warning("creating");
            e.printStackTrace();
        }
    }

    /**
     * Loads the file
     */
    public void load() {
        load((AnnoyingPlugin) null);
    }

    /**
     * Creates the file
     */
    public void create() {
        create(null);
    }


    /**
     * Sets a value in the {@link #getYaml()}
     *
     * @param   path    the path to the value
     * @param   value   the value to set
     * @param   save    whether to save the file after setting the value
     */
    public void set(@NotNull String path, @Nullable Object value, boolean save) {
        set(path, value, null, save);
    }

    /**
     * Saves the file
     */
    public void save() {
        save((AnnoyingPlugin) null);
    }
}
