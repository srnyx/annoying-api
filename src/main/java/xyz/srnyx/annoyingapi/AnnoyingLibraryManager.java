package xyz.srnyx.annoyingapi;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.classloader.IsolatedClassLoader;

import org.jetbrains.annotations.NotNull;

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
     * Load a {@link RuntimeLibrary} into the server's classpath or into an isolated classloader
     * <br>If the library is isolated, the classloader is returned
     *
     * @param   runtimeLibrary  the library to load
     *
     * @return                  the classloader of the library if it is isolated
     */
    @NotNull
    public Optional<IsolatedClassLoader> loadLibrary(@NotNull RuntimeLibrary runtimeLibrary) {
        for (final String repository : runtimeLibrary.repositories) addRepository(repository);
        final Library library = runtimeLibrary.getLibrary(plugin);
        loadLibrary(library);
        if (!library.isIsolatedLoad()) {
            loadedLibraries.add(runtimeLibrary);
            return Optional.empty();
        }
        return getIsolatedClassLoaderOf(runtimeLibrary);
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
        return Optional.ofNullable(getIsolatedClassLoaderOf(library.id));
    }

    /**
     * Check if a {@link RuntimeLibrary} is loaded
     *
     * @param   library     the library to check
     * @param   isolated    whether to check if the library is loaded into an isolated classloader (use {@link #getIsolatedClassLoaderOf(RuntimeLibrary)} to get the classloader)
     *
     * @return              whether the library is loaded
     *
     * @see                 #isLoaded(RuntimeLibrary)
     */
    public boolean isLoaded(@NotNull RuntimeLibrary library, boolean isolated) {
        return isolated ? getIsolatedClassLoaderOf(library).isPresent() : loadedLibraries.contains(library);
    }

    /**
     * Check if a {@link RuntimeLibrary} is loaded in the server's classpath
     *
     * @param   library the library to check
     *
     * @return          whether the library is loaded
     *
     * @see             #isLoaded(RuntimeLibrary, boolean)
     */
    public boolean isLoaded(@NotNull RuntimeLibrary library) {
        return isLoaded(library, false);
    }
}
