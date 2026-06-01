package xyz.srnyx.annoyingapi.stats.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.library.AnnoyingLibrary;
import xyz.srnyx.annoyingapi.stats.loader.StatsLoader;
import xyz.srnyx.annoyingapi.parents.Registrable;

import java.util.Collection;
import java.util.logging.Level;


public abstract class StatsProvider<L extends StatsLoader<?, ?>> extends Registrable {
    @Nullable public L loader;

    @NotNull
    public abstract L createLoader();

    @Nullable
    public Collection<AnnoyingLibrary> getRequiredLibraries() {
        return null;
    }

    @Override
    public void register() {
        final AnnoyingPlugin plugin = getAnnoyingPlugin();

        // Load required libraries
        final Collection<AnnoyingLibrary> libraries = getRequiredLibraries();
        if (libraries != null) for (final AnnoyingLibrary library : libraries) {
            if (!plugin.libraryManager.loadLibrary(library)) {
                AnnoyingPlugin.log(Level.WARNING, "Failed to load required library " + library.getId() + " for stats provider " + getClass().getSimpleName());
                return;
            }
        }

        // Create loader
        try {
            loader = createLoader();
            loader.load();
        } catch (final Exception e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to create stats loader for provider " + getClass().getSimpleName(), e);
            return;
        }

        super.register();
    }

    @Override
    public void unregister() {
        if (loader != null) {
            loader.unload();
            loader = null;
        }
        super.unregister();
    }
}
