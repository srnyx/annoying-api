package xyz.srnyx.annoyingapi;

import com.google.common.collect.ImmutableSortedSet;

import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.SortedSet;
import java.util.function.Function;


/**
 * All possible runtime libraries that Annoying API may download (depending on feature use)
 * <br>Not all libraries are downloaded/loaded at the same time, only once needed!
 */
public enum RuntimeLibrary {
    /**
     * org.bstats:bstats-base
     */
    BSTATS_BASE("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("org{}bstats")
            .artifactId("bstats-base")
            .version("3.0.2")
            .relocate(plugin.getRelocation("org{}bstats")).build()),
    /**
     * org.bstats:bstats-bukkit
     */
    BSTATS_BUKKIT("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("org{}bstats")
            .artifactId("bstats-bukkit")
            .version("3.0.2")
            .relocate(plugin.getRelocation("org{}bstats")).build()),
    /**
     * org.javassist:javassist
     */
    JAVASSIST("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("org{}javassist")
            .artifactId("javassist")
            .version("3.28.0-GA")
            .relocate(plugin.getRelocation("javassist{}", "javassist{}")).build()),
    /**
     * org.reflections:reflections
     */
    REFLECTIONS("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("org{}reflections")
            .artifactId("reflections")
            .version("0.10.2")
            .relocate(plugin.getRelocation("javassist{}", "javassist{}"))
            .relocate(plugin.getRelocation("org{}reflections")).build()),
    /**
     * de.tr7zw:item-nbt-api
     */
    ITEM_NBT_API("https://repo.codemc.org/repository/maven-public/", plugin -> Library.builder()
            .groupId("de{}tr7zw")
            .artifactId("item-nbt-api")
            .version("2.13.2")
            .relocate(plugin.getRelocation("de{}tr7zw{}changeme{}nbtapi")).build()),
    /**
     * com.h2database:h2
     */
    H2("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("com{}h2database")
            .artifactId("h2")
            .version("2.2.220")
            .relocate(plugin.getRelocation("org{}h2")).build()),
    /**
     * org.postgresql:postgresql
     */
    POSTGRESQL("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("org{}postgresql")
            .artifactId("postgresql")
            .version("42.7.3")
            .relocate(plugin.getRelocation("org{}postgresql")).build());

    /**
     * The repositories to add before loading the library (immutable)
     */
    @NotNull public final SortedSet<String> repositories;
    /**
     * The library to load
     */
    @NotNull public final Function<AnnoyingPlugin, Library> library;

    /**
     * Creates a new {@link RuntimeLibrary}
     *
     * @param   repositories    {@link #repositories}
     * @param   library         {@link #library}
     */
    RuntimeLibrary(@NotNull Collection<String> repositories, @NotNull Function<AnnoyingPlugin, Library> library) {
        this.repositories = ImmutableSortedSet.copyOf(repositories);
        this.library = library;
    }

    /**
     * Creates a new {@link RuntimeLibrary} with a single repository
     *
     * @param   repository  the repository to add
     * @param   library     the library to load
     */
    RuntimeLibrary(@NotNull String repository, @NotNull Function<AnnoyingPlugin, Library> library) {
        this.repositories = ImmutableSortedSet.of(repository);
        this.library = library;
    }

    /**
     * Gets the library to load
     *
     * @param   plugin  the plugin to load the library into
     *
     * @return          the library to load
     */
    @NotNull
    public Library getLibrary(@NotNull AnnoyingPlugin plugin) {
        return library.apply(plugin);
    }

    /**
     * Downloads and loads the library into the specified plugin
     *
     * @param   plugin  the plugin to load the library into
     */
    public void load(@NotNull AnnoyingPlugin plugin) {
        final LibraryManager manager = plugin.libraryManager;
        for (final String repository : repositories) manager.addRepository(repository);
        manager.loadLibrary(getLibrary(plugin));
        plugin.loadedLibraries.add(this);
    }
}
