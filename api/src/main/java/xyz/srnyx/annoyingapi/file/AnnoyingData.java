package xyz.srnyx.annoyingapi.file;

import org.jetbrains.annotations.NotNull;

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
     * @param   plugin      the plugin that is creating the file
     * @param   path        the path to the file (relative to {@code data} folder in the plugin's folder)
     * @param   canBeEmpty  whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save()} is used
     */
    public AnnoyingData(@NotNull AnnoyingPlugin plugin, @NotNull String path, boolean canBeEmpty) {
        super(plugin, path, "data/", canBeEmpty);
    }

    /**
     * Constructs and loads a new {@link AnnoyingData} from the path with {@code canBeEmpty} set to {@code true}
     *
     * @param   plugin  the plugin that is creating the file
     * @param   path    the path to the file (inside {@code /data/})
     */
    public AnnoyingData(@NotNull AnnoyingPlugin plugin, @NotNull String path) {
        this(plugin, path, true);
    }

    @Override
    public void create() {
        final Path filePath = file.toPath();
        try {
            Files.createDirectories(filePath.getParent());
            Files.createFile(filePath);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
