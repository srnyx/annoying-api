package xyz.srnyx.annoyingapi.library;

import net.byteflux.libby.Library;
import net.byteflux.libby.relocation.Relocation;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * A library that can be loaded into the server's classpath or into an isolated classloader using {@link AnnoyingLibraryManager}
 * <br>Not all libraries are downloaded/loaded at the same time, only once needed!
 * <br>
 * <br><b>If you are implementing this interface for a class (rather than an enum), you must override {@link #getId()}!</b>
 */
public interface AnnoyingLibrary {
    /**
     * The unique ID of the library (used for identification with {@link AnnoyingLibraryManager#getIsolatedClassLoaderOf(RuntimeLibrary)})
     *
     * @return  the unique ID of the library
     */
    @NotNull
    default String getId() {
        return getClass().getSimpleName() + "." + ((Enum<?>) this).name();
    }

    /**
     * The supplier of the builder to create the library with
     *
     * @return  a supplier that gives a new builder to create the library with
     */
    @NotNull
    Supplier<Library.Builder> getLibrarySupplier();

    /**
     * The relocations to apply to the library
     *
     * @return  a function that gives a collection of relocations to apply to the library
     */
    @NotNull
    Function<AnnoyingPlugin, Collection<Relocation>> getRelocations();

    /**
     * Sets the {@link #getId() ID} of the library and returns the builder
     *
     * @return  the builder to create the library with
     */
    @NotNull
    default Library.Builder getLibrary() {
        return getLibrarySupplier().get().id(getId());
    }

    /**
     * Runs {@link #getLibrary()} and applies {@link #getRelocations() the relocations} to the library
     *
     * @param   plugin  the plugin to apply the relocations with
     *
     * @return          the builder to create the library with relocations
     */
    @NotNull
    default Library.Builder getLibraryWithRelocations(@NotNull AnnoyingPlugin plugin) {
        final Library.Builder builder = getLibrary();
        for (final Relocation relocation : getRelocations().apply(plugin)) builder.relocate(relocation);
        return builder;
    }
}
