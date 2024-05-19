package xyz.srnyx.annoyingapi.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Represents a file in the plugin's folder (in {@code /data/})
 */
public class AnnoyingData extends AnnoyingFile {
    /**
     * Constructs and loads a new {@link AnnoyingData} from the path
     *
     * @param   plugin      {@link #plugin}
     * @param   path        the path to the file (relative to {@code data} folder in the plugin's folder)
     * @param   options {@link #fileOptions}
     */
    public AnnoyingData(@NotNull AnnoyingPlugin plugin, @NotNull String path, @Nullable AnnoyingFile.Options<?> options) {
        super(plugin, "data/" + path, options);
    }

    /**
     * Constructs and loads a new {@link AnnoyingData} from the path with {@code canBeEmpty} set to {@code true}
     *
     * @param   plugin  {@link #plugin}
     * @param   path    the path to the file (relative to {@code data} folder in the plugin's folder)
     */
    public AnnoyingData(@NotNull AnnoyingPlugin plugin, @NotNull String path) {
        this(plugin, path, null);
    }

    @Override
    public void create() {
        final Path filePath = file.toPath();
        plugin.attemptAsync(() -> {
            try {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });
    }
}
