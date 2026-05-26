package xyz.srnyx.annoyingapi.stats.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.library.AnnoyingLibrary;
import xyz.srnyx.annoyingapi.stats.loader.StatsLoader;

import java.util.List;
import java.util.logging.Level;


//TODO turn this into a Registrable so that we can use library-specific classes/methods and then combine Loader into Provider
public abstract class StatsProvider<I, P extends StatsProvider<I, P, ?>, L extends StatsLoader<P, ?>> {
    /**
     * <i>{@code REQUIRED}</i> The ID of the plugin on the service provider
     */
    @NotNull public I id;

    /**
     * Constructs a new provider with the given ID
     */
    public StatsProvider(@NotNull I id) {
        this.id = id;
    }

    @NotNull
    public abstract Class<L> getLoaderClass();

    @Nullable
    public L createLoader(@NotNull AnnoyingPlugin plugin) {
        try {
            if (!loadRequiredLibraries(plugin)) return null;
            final L loader = getLoaderClass().getDeclaredConstructor().newInstance();
            loader.load(plugin, (P) this);
            return loader;
        } catch (final Exception e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to create stats loader for provider " + getClass().getSimpleName(), e);
            return null;
        }
    }

    @Nullable
    public List<AnnoyingLibrary> getRequiredLibraries() {
        return null;
    }

    public boolean loadRequiredLibraries(@NotNull AnnoyingPlugin plugin) {
        final List<AnnoyingLibrary> libraries = getRequiredLibraries();
        if (libraries != null) for (final AnnoyingLibrary library : libraries) {
            if (!plugin.libraryManager.loadLibrary(library)) {
                AnnoyingPlugin.log(Level.WARNING, "Failed to load required library " + library.getId() + " for stats provider " + getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }
}
