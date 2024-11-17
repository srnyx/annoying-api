package xyz.srnyx.annoyingapi.library;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.classloader.IsolatedClassLoader;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


/**
 * A library manager that can load libraries into the server's classpath or into an isolated classloader using {@link RuntimeLibrary}
 */
public class AnnoyingLibraryManager extends BukkitLibraryManager implements Annoyable {
    /**
     * The {@link AnnoyingPlugin plugin} that this library manager is for
     */
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * Set of loaded {@link RuntimeLibrary libraries} that are <b>not</b> isolated
     */
    @NotNull private final Set<RuntimeLibrary> loadedLibraries = new HashSet<>();

    /**
     * Create a new {@link AnnoyingLibraryManager} for the plugin
     *
     * @param   plugin  {@link #plugin}
     */
    public AnnoyingLibraryManager(@NotNull AnnoyingPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    /**
     * Create a new {@link AnnoyingLibraryManager} for the plugin with a custom directory name
     *
     * @param   plugin          {@link #plugin}
     * @param   directoryName   the name of the directory to store the libraries in
     */
    public AnnoyingLibraryManager(@NotNull AnnoyingPlugin plugin, @NotNull String directoryName) {
        super(plugin, directoryName);
        this.plugin = plugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    /**
     * Load a {@link RuntimeLibrary} into the server's classpath
     *
     * @param   library the library to load
     *
     * @see             #loadLibraryIsolated(RuntimeLibrary)
     */
    public void loadLibrary(@NotNull RuntimeLibrary library) {
        loadLibrary(library.getLibraryWithRelocations(plugin).build());
        loadedLibraries.add(library);
    }

    /**
     * Load a {@link RuntimeLibrary} into an isolated classloader
     *
     * @param   library the library to load
     *
     * @return          the isolated classloader containing the library
     *
     * @see             #loadLibrary(RuntimeLibrary)
     */
    @NotNull
    public IsolatedClassLoader loadLibraryIsolated(@NotNull RuntimeLibrary library) {
        loadLibrary(library.getLibrary().isolatedLoad(true).build());
        return getIsolatedClassLoaderOf(library).orElseThrow(() -> new IllegalStateException("Failed to load isolated library"));
    }

    /**
     * Get the {@link IsolatedClassLoader} of a {@link RuntimeLibrary}
     *
     * @param   library the library to get the classloader of
     *
     * @return          the classloader of the library
     */
    @NotNull
    public Optional<IsolatedClassLoader> getIsolatedClassLoaderOf(@NotNull RuntimeLibrary library) {
        return Optional.ofNullable(getIsolatedClassLoaderOf(library.getId()));
    }

    /**
     * Check if a {@link RuntimeLibrary} is loaded in the server's classpath
     *
     * @param   library the library to check
     *
     * @return          whether the library is loaded
     *
     * @see             #isLoadedIsolated(RuntimeLibrary)
     */
    public boolean isLoaded(@NotNull RuntimeLibrary library) {
        return loadedLibraries.contains(library);
    }

    /**
     * Check if a {@link RuntimeLibrary} is loaded in an isolated classloader
     *
     * @param   library the library to check
     *
     * @return          whether the library is loaded
     *
     * @see             #isLoaded(RuntimeLibrary)
     */
    public boolean isLoadedIsolated(@NotNull RuntimeLibrary library) {
        return getIsolatedClassLoaderOf(library).isPresent();
    }
}