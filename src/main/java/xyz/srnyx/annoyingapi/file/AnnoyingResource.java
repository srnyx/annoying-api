package xyz.srnyx.annoyingapi.file;

import org.bukkit.configuration.file.YamlConfiguration;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.File;


/**
 * Represents a file in the plugin's folder (usually a config file)
 */
public class AnnoyingResource extends YamlConfiguration implements AnnoyingFile {
    @NotNull private final AnnoyingPlugin plugin;
    @NotNull private final String path;
    @NotNull private final File file;
    private final boolean canBeEmpty;

    /**
     * Constructs and loads a new {@link AnnoyingResource} from the path
     *
     * @param   path        the path to the file (relative to the plugin's folder)
     * @param   canBeEmpty  whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save()} is used
     * @param   plugin      the {@link AnnoyingPlugin} instance (used in {@link #load()})
     */
    public AnnoyingResource(@NotNull AnnoyingPlugin plugin, @NotNull String path, boolean canBeEmpty) {
        this.plugin = plugin;
        this.path = path;
        this.file = new File(plugin.getDataFolder(), path);
        this.canBeEmpty = canBeEmpty;
        load();
    }

    /**
     * Constructs and loads a new {@link AnnoyingResource} from the path
     *
     * @param   path    the path to the file (relative to the plugin's folder)
     * @param   plugin  the {@link AnnoyingPlugin} instance (used in {@link #load()})
     */
    public AnnoyingResource(@NotNull AnnoyingPlugin plugin, @NotNull String path) {
        this(plugin, path, true);
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
    public void create() {
        plugin.saveResource(path, false);
    }
}
