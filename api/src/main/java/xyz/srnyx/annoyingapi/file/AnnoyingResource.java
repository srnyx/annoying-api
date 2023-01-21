package xyz.srnyx.annoyingapi.file;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.File;


/**
 * Represents a file in the plugin's folder (usually a config file)
 */
public class AnnoyingResource extends AnnoyingFile {
    @NotNull private final AnnoyingPlugin plugin;

    /**
     * Constructs and loads a new {@link AnnoyingResource} from the path
     *
     * @param   path        the path to the file (relative to the plugin's folder)
     * @param   canBeEmpty  whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save()} is used
     * @param   plugin      the {@link AnnoyingPlugin} instance (used in {@link #load()})
     */
    public AnnoyingResource(@NotNull AnnoyingPlugin plugin, @NotNull String path, boolean canBeEmpty) {
        super(path, new File(plugin.getDataFolder(), path), canBeEmpty);
        this.plugin = plugin;
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

    @Override
    public void create() {
        plugin.saveResource(super.path, false);
    }
}
