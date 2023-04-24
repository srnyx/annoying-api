package xyz.srnyx.annoyingapi.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


/**
 * Represents a file in the plugin's folder (usually a config file), the file MUST exist in the {@code resources} folder
 */
public class AnnoyingResource extends AnnoyingFile {
    /**
     * Constructs and loads a new {@link AnnoyingResource} from the path, the file MUST exist in the {@code resources} folder
     *
     * @param   plugin  the {@link AnnoyingPlugin} instance (used in {@link #load()})
     * @param   path    the path to the file (relative to the plugin's folder)
     * @param   resourceOptions the {@link ResourceOptions options} for the file
     */
    public AnnoyingResource(@NotNull AnnoyingPlugin plugin, @NotNull String path, @Nullable ResourceOptions resourceOptions) {
        super(plugin, path, resourceOptions);
        resourceOptions = resourceOptions == null ? new ResourceOptions() : resourceOptions;

        // Create default file
        if (resourceOptions.createDefaultFile) try {
            final InputStream input = plugin.getResource(path);
            if (input == null) return;
            final Path defaultPath = plugin.getDataFolder().toPath().resolve("default/" + path);
            Files.createDirectories(defaultPath.getParent());
            Files.copy(input, defaultPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs and loads a new {@link AnnoyingResource} from the path, the file MUST exist in the {@code resources} folder
     *
     * @param   plugin  the {@link AnnoyingPlugin} instance (used in {@link #load()})
     * @param   path    the path to the file (relative to the plugin's folder)
     *
     * @see #AnnoyingResource(AnnoyingPlugin, String, ResourceOptions)
     */
    public AnnoyingResource(@NotNull AnnoyingPlugin plugin, @NotNull String path) {
        this(plugin, path, new ResourceOptions());
    }

    @Override
    public void create() {
        plugin.saveResource(path, false);
    }

    /**
     * Represents the options for the {@link AnnoyingResource}
     */
    public static class ResourceOptions extends AnnoyingFile.FileOptions {
        /**
         * Whether to create an up-to-date default file called {@code default_}{@link File#getName() name}
         */
        public boolean createDefaultFile = true;

        /**
         * Sets {@link #createDefaultFile}
         *
         * @param   createDefaultFile   the new value
         *
         * @return                      this
         */
        @NotNull
        public ResourceOptions createDefaultFile(boolean createDefaultFile) {
            this.createDefaultFile = createDefaultFile;
            return this;
        }
    }
}
