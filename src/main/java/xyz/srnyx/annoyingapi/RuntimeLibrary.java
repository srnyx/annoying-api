package xyz.srnyx.annoyingapi;

import com.google.common.collect.ImmutableSortedSet;

import net.byteflux.libby.Library;

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
     * {@code org.bstats:bstats-base}
     */
    BSTATS_BASE("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("org{}bstats")
            .artifactId("bstats-base")
            .version("3.1.0")
            .relocate(plugin.getRelocation("org{}bstats"))),
    /**
     * {@code org.bstats:bstats-bukkit}
     */
    BSTATS_BUKKIT("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("org{}bstats")
            .artifactId("bstats-bukkit")
            .version("3.1.0")
            .relocate(plugin.getRelocation("org{}bstats"))),
    /**
     * {@code org.javassist:javassist}
     */
    JAVASSIST("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("org{}javassist")
            .artifactId("javassist")
            .version("3.28.0-GA")
            .relocate(plugin.getRelocation("javassist{}", "javassist{}"))),
    /**
     * {@code org.reflections:reflections}
     */
    REFLECTIONS("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("org{}reflections")
            .artifactId("reflections")
            .version("0.10.2")
            .relocate(plugin.getRelocation("javassist{}", "javassist{}"))
            .relocate(plugin.getRelocation("org{}reflections"))),
    /**
     * {@code de.tr7zw:item-nbt-api}
     */
    ITEM_NBT_API("https://repo.codemc.org/repository/maven-public/", plugin -> Library.builder()
            .groupId("de{}tr7zw")
            .artifactId("item-nbt-api")
            .version("2.13.2")
            .relocate(plugin.getRelocation("de{}tr7zw{}changeme{}nbtapi"))),
    /**
     * {@code com.h2database:h2}
     * <br>Isolated load by default
     */
    H2("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("com{}h2database")
            .artifactId("h2")
            .version("2.2.220")
            .isolatedLoad(true)),
    /**
     * {@code org.postgresql:postgresql}
     */
    POSTGRESQL("https://repo1.maven.org/maven2/", plugin -> Library.builder()
            .groupId("org{}postgresql")
            .artifactId("postgresql")
            .version("42.7.3")
            .relocate(plugin.getRelocation("org{}postgresql")));

    /**
     * The unique ID of the library (used for identification with {@link AnnoyingLibraryManager#getIsolatedClassLoaderOf(RuntimeLibrary)})
     */
    @NotNull public final String id;
    /**
     * The repositories to add before loading the library (immutable)
     */
    @NotNull public final SortedSet<String> repositories;
    /**
     * The builder to create the library with
     */
    @NotNull public final Function<AnnoyingPlugin, Library.Builder> libraryBuilder;

    /**
     * Creates a new {@link RuntimeLibrary}
     *
     * @param   repositories    {@link #repositories}
     * @param   libraryBuilder  {@link #libraryBuilder}
     */
    RuntimeLibrary(@NotNull Collection<String> repositories, @NotNull Function<AnnoyingPlugin, Library.Builder> libraryBuilder) {
        this.id = getClass().getSimpleName() + "." + name();
        this.repositories = ImmutableSortedSet.copyOf(repositories);
        this.libraryBuilder = plugin -> libraryBuilder.apply(plugin).id(id);
    }

    /**
     * Creates a new {@link RuntimeLibrary} with a single repository
     *
     * @param   repository      {@link #repositories}
     * @param   libraryBuilder  {@link #libraryBuilder}
     */
    RuntimeLibrary(@NotNull String repository, @NotNull Function<AnnoyingPlugin, Library.Builder> libraryBuilder) {
        this(ImmutableSortedSet.of(repository), libraryBuilder);
    }

    /**
     * Builds and returns the library to load from the {@link #libraryBuilder}
     *
     * @param   plugin  the plugin to build the library with
     *
     * @return          the library to build/get
     */
    @NotNull
    public Library getLibrary(@NotNull AnnoyingPlugin plugin) {
        return libraryBuilder.apply(plugin).build();
    }
}
