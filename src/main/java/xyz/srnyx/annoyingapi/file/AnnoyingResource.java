package xyz.srnyx.annoyingapi.file;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


/**
 * Represents a file in the plugin's folder (usually a config file), the file MUST exist in the {@code resources} folder
 */
public class AnnoyingResource extends AnnoyingFile<AnnoyingResource> {
    /**
     * The path to the file (relative to the plugin's folder)
     */
    @NotNull private final String path;

    /**
     * Constructs and loads a new {@link AnnoyingResource} from the path, the file MUST exist in the {@code resources} folder
     *
     * @param   plugin      the {@link AnnoyingPlugin} instance (used in {@link #load()})
     * @param   path        the path to the file (relative to the plugin's folder)
     * @param   fileOptions the {@link Options options} for the file
     */
    public AnnoyingResource(@NotNull AnnoyingPlugin plugin, @NotNull String path, @Nullable AnnoyingResource.Options fileOptions) {
        super(plugin, path, fileOptions);
        this.path = path;
        fileOptions = fileOptions == null ? new Options() : fileOptions;
        load();

        // Create default file
        if (fileOptions.createDefaultFile) {
            final InputStream input = plugin.getResource(path);
            if (input == null) return;
            final Path defaultPath = plugin.getDataFolder().toPath().resolve("default/" + path);
            plugin.attemptAsync(() -> {
                try {
                    Files.createDirectories(defaultPath.getParent());
                    Files.copy(input, defaultPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Constructs and loads a new {@link AnnoyingResource} from the path, the file MUST exist in the {@code resources} folder
     *
     * @param   plugin  the {@link AnnoyingPlugin} instance (used in {@link #load()})
     * @param   path    the path to the file (relative to the plugin's folder)
     *
     * @see #AnnoyingResource(AnnoyingPlugin, String, Options)
     */
    public AnnoyingResource(@NotNull AnnoyingPlugin plugin, @NotNull String path) {
        this(plugin, path, null);
    }

    @Override
    public void create() {
        plugin.saveResource(path, false);
    }

    /**
     * Represents the options for the {@link AnnoyingResource}
     * <br>
     * <br><b>Default options:</b>
     * <ul>
     *     <li>{@link #createDefaultFile} = {@code true}
     *     <li><i>rest: {@link AnnoyingFile.Options}</i>
     * </ul>
     */
    public static class Options extends AnnoyingFile.Options<Options> {
        /**
         * Whether to create an up-to-date default file in the {@code default} folder
         */
        public boolean createDefaultFile = true;

        /**
         * Constructs a new {@link Options} with the default values
         */
        public Options() {
            // Only exists to give the constructor a Javadoc
        }

        /**
         * Loads the {@link Options} from the {@link ConfigurationSection}
         *
         * @param   section the section to load from
         *
         * @return          the loaded {@link Options}
         */
        @NotNull
        public static AnnoyingResource.Options load(@NotNull ConfigurationSection section) {
            final Options options = AnnoyingFile.Options.load(new Options(), section);
            if (section.contains("createDefaultFile")) options.createDefaultFile = section.getBoolean("createDefaultFile");
            return options;
        }

        /**
         * Sets {@link #createDefaultFile}
         *
         * @param   createDefaultFile   the new value
         *
         * @return                      this
         */
        @NotNull
        public AnnoyingResource.Options createDefaultFile(boolean createDefaultFile) {
            this.createDefaultFile = createDefaultFile;
            return this;
        }

        @Override @NotNull
        public String toString() {
            return "ResourceOptions{canBeEmpty=" + canBeEmpty + ",createDefaultFile=" + createDefaultFile + '}';
        }
    }
}
