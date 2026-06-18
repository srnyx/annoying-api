package xyz.srnyx.annoyingapi.library;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.classloader.IsolatedClassLoader;
import net.byteflux.libby.relocation.Relocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import java.util.*;
import java.util.function.Function;
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
     * Load {@link AnnoyingLibrary libraries} into the server's classpath
     * <br>This will stop loading libraries if one fails
     *
     * @param   libraries   the libraries to load
     *
     * @return  true if all libraries were loaded successfully, false if ANY failed to load
     *
     * @see #loadLibraryIsolated(AnnoyingLibrary)
     */
    public boolean loadLibrary(@NotNull Iterable<AnnoyingLibrary> libraries) {
        for (final AnnoyingLibrary library : libraries) {
            // Load required libraries and get all relocations
            final Collection<Relocation> dependencyRelocations = new HashSet<>();
            final Collection<AnnoyingLibrary> requiredLibraries = library.getRequiredLibraries();
            if (requiredLibraries != null) for (final AnnoyingLibrary required : requiredLibraries) {
                // Load required library
                if (!isLoaded(required) && !loadLibrary(required)) {
                    plugin.logErrorTrack(Level.SEVERE, "Failed to load required library " + required.getId() + " for " + library.getId());
                    return false;
                }

                // Get relocations from required library (deep search)
                dependencyRelocations.addAll(getRelocationsDeep(required, new HashSet<>()));
            }

            // Get library builder with relocations
            final Library.Builder builder = library.getLibraryWithRelocations(plugin);
            for (final Relocation relocation : dependencyRelocations) builder.relocate(relocation);

            // Load library
            try {
                loadLibrary(builder.build());
                loadedLibraries.add(library);
            } catch (final Exception e) {
                plugin.logErrorTrack(Level.SEVERE, "&cFailed to load library &4" + library.getId(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * @see #loadLibrary(Iterable)
     */
    public boolean loadLibrary(@NotNull AnnoyingLibrary @NotNull ... libraries) {
        return loadLibrary(Arrays.asList(libraries));
    }

    @NotNull
    private Collection<Relocation> getRelocationsDeep(@NotNull AnnoyingLibrary library, @NotNull Set<AnnoyingLibrary> visited) {
        final Collection<Relocation> relocations = new HashSet<>();

        // Prevent infinite recursion
        if (!visited.add(library)) return relocations;

        // Add own relocations
        final Function<AnnoyingPlugin, Collection<Relocation>> ownRelocationsFunction = library.getRelocations();
        if (ownRelocationsFunction != null) {
            final Collection<Relocation> ownRelocations = library.getRelocations().apply(plugin);
            if (ownRelocations != null) relocations.addAll(ownRelocations);
        }

        // Add relocations of required libraries (deep search)
        final Collection<AnnoyingLibrary> requiredLibraries = library.getRequiredLibraries();
        if (requiredLibraries != null) for (final AnnoyingLibrary required : requiredLibraries) {
            relocations.addAll(getRelocationsDeep(required, visited));
        }

        return relocations;
    }

    /**
     * Load a {@link AnnoyingLibrary} into an isolated classloader
     * <br><b>{@link AnnoyingLibrary#getRequiredLibraries()} is IGNORED when loading isolated libraries, as it is currently not possible to load multiple libraries into the same isolated classloader!</b>
     *
     * @param   library the library to load
     *
     * @return  the isolated classloader containing the library, or null if the library failed to load
     *
     * @see #loadLibrary(AnnoyingLibrary...)
     */
    @Nullable
    public IsolatedClassLoader loadLibraryIsolated(@NotNull AnnoyingLibrary library) {
        // Load library into isolated classloader
        try {
            loadLibrary(library.getLibrary().isolatedLoad(true).build());
        } catch (final Exception e) {
            plugin.logErrorTrack(Level.SEVERE, "&cFailed to load isolated library &4" + library.getId(), e);
            return null;
        }

        // Return the isolated class loader
        final IsolatedClassLoader classLoader = getIsolatedClassLoaderOf(library).orElse(null);
        if (classLoader == null) plugin.logErrorTrack(Level.SEVERE, "&cFailed to get classloader of isolated library &4" + library.getId() + " &cafter loading");
        return classLoader;
    }

    /**
     * Load {@link AnnoyingLibrary libraries} into the server's classpath if they're not already loaded
     * <br>This will stop loading libraries if one fails
     *
     * @param   libraries   the libraries to load
     *
     * @return  true if all libraries were loaded successfully, false if ANY failed to load
     */
    public boolean loadIfNotLoaded(@NotNull AnnoyingLibrary @NotNull ... libraries) {
        for (final AnnoyingLibrary library : libraries) {
            if (!isLoaded(library) && !loadLibrary(library)) return false;
        }
        return true;
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
