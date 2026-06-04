package xyz.srnyx.annoyingapi.library;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.classloader.IsolatedClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;


/**
 * A library manager that can load libraries into the server's classpath or into an isolated classloader using {@link AnnoyingLibrary}
 */
public class AnnoyingLibraryManager extends BukkitLibraryManager implements Annoyable {
    /**
     * The {@link AnnoyingPlugin plugin} that this library manager is for
     */
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * Set of loaded {@link AnnoyingLibrary libraries} that are <b>not</b> isolated
     */
    @NotNull private final Set<AnnoyingLibrary> loadedLibraries = new HashSet<>();

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
     * Load a {@link AnnoyingLibrary} into the server's classpath
     *
     * @param   library the library to load
     *
     * @return          whether the library was loaded successfully
     *
     * @see             #loadLibraryIsolated(AnnoyingLibrary)
     */
    public boolean loadLibrary(@NotNull AnnoyingLibrary library) {
        try {
            loadLibrary(library.getLibraryWithRelocations(plugin).build());
            loadedLibraries.add(library);
            return true;
        } catch (final Exception e) {
            AnnoyingPlugin.log(Level.SEVERE, "&cFailed to load library &4" + library.getId(), e);
            return false;
        }
    }

    /**
     * Load a {@link AnnoyingLibrary} into an isolated classloader
     *
     * @param   library the library to load
     *
     * @return          the isolated classloader containing the library, or null if the library failed to load
     *
     * @see             #loadLibrary(AnnoyingLibrary)
     */
    @Nullable
    public IsolatedClassLoader loadLibraryIsolated(@NotNull AnnoyingLibrary library) {
        // Load library into isolated classloader
        try {
            loadLibrary(library.getLibrary().isolatedLoad(true).build());
        } catch (final Exception e) {
            AnnoyingPlugin.log(Level.SEVERE, "&cFailed to load isolated library &4" + library.getId(), e);
            return null;
        }

        // Return the isolated class loader
        final IsolatedClassLoader classLoader = getIsolatedClassLoaderOf(library).orElse(null);
        if (classLoader == null) AnnoyingPlugin.log(Level.SEVERE, "&cFailed to get classloader of isolated library &4" + library.getId() + " &cafter loading");
        return classLoader;
    }

    /**
     * Load a {@link AnnoyingLibrary} into the server's classpath if it's not already loaded
     *
     * @param   library the library to load
     *
     * @return  whether the library is now loaded
     */
    public boolean loadIfNotLoaded(@NotNull AnnoyingLibrary library) {
        return isLoaded(library) || loadLibrary(library);
    }

    /**
     * Load a {@link AnnoyingLibrary} into an isolated classloader if it's not already loaded
     *
     * @param   library the library to load
     *
     * @return  the isolated classloader containing the library, or null if the library failed to load
     */
    @Nullable
    public IsolatedClassLoader loadIfNotLoadedIsolated(@NotNull AnnoyingLibrary library) {
        return getIsolatedClassLoaderOf(library).orElseGet(() -> loadLibraryIsolated(library));
    }

    /**
     * Get the {@link IsolatedClassLoader} of a {@link AnnoyingLibrary}
     *
     * @param   library the library to get the classloader of
     *
     * @return          the classloader of the library
     */
    @NotNull
    public Optional<IsolatedClassLoader> getIsolatedClassLoaderOf(@NotNull AnnoyingLibrary library) {
        return Optional.ofNullable(getIsolatedClassLoaderOf(library.getId()));
    }

    /**
     * Check if a {@link AnnoyingLibrary} is loaded in the server's classpath
     *
     * @param   library the library to check
     *
     * @return          whether the library is loaded
     *
     * @see             #isLoadedIsolated(AnnoyingLibrary)
     */
    public boolean isLoaded(@NotNull AnnoyingLibrary library) {
        return loadedLibraries.contains(library);
    }

    /**
     * Check if a {@link AnnoyingLibrary} is loaded in an isolated classloader
     *
     * @param   library the library to check
     *
     * @return          whether the library is loaded
     *
     * @see             #isLoaded(AnnoyingLibrary)
     */
    public boolean isLoadedIsolated(@NotNull AnnoyingLibrary library) {
        return getIsolatedClassLoaderOf(library).isPresent();
    }
}
