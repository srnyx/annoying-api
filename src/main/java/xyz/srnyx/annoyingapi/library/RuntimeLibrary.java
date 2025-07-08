package xyz.srnyx.annoyingapi.library;

import net.byteflux.libby.Library;
import net.byteflux.libby.Repositories;
import net.byteflux.libby.relocation.Relocation;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * All possible runtime libraries that Annoying API may download (depending on feature use)
 */
public enum RuntimeLibrary implements AnnoyingLibrary {
    /**
     * {@code org.bstats:bstats-base}
     */
    BSTATS_BASE(
            () -> Library.builder()
                    .repository(Repositories.MAVEN_CENTRAL)
                    .groupId("org{}bstats")
                    .artifactId("bstats-base")
                    .version("3.1.0"),
            plugin -> Collections.singleton(plugin.getRelocation("org{}bstats"))),
    /**
     * {@code org.bstats:bstats-bukkit}
     */
    BSTATS_BUKKIT(
            () -> Library.builder()
                    .repository(Repositories.MAVEN_CENTRAL)
                    .groupId("org{}bstats")
                    .artifactId("bstats-bukkit")
                    .version("3.1.0"),
            plugin -> Collections.singleton(plugin.getRelocation("org{}bstats"))),
    /**
     * {@code org.javassist:javassist}
     */
    JAVASSIST(
            () -> Library.builder()
                    .repository(Repositories.MAVEN_CENTRAL)
                    .groupId("org{}javassist")
                    .artifactId("javassist")
                    .version("3.28.0-GA"),
            plugin -> Collections.singleton(plugin.getRelocation("javassist{}", "javassist{}"))),
    /**
     * {@code org.reflections:reflections}
     */
    REFLECTIONS(
            () -> Library.builder()
                    .repository(Repositories.MAVEN_CENTRAL)
                    .groupId("org{}reflections")
                    .artifactId("reflections")
                    .version("0.10.2"),
            plugin -> Arrays.asList(
                    plugin.getRelocation("javassist{}", "javassist{}"),
                    plugin.getRelocation("org{}reflections"))),
    /**
     * {@code de.tr7zw:item-nbt-api}
     */
    ITEM_NBT_API(
            () -> Library.builder()
                    .repository("https://repo.codemc.org/repository/maven-public/")
                    .groupId("de{}tr7zw")
                    .artifactId("item-nbt-api")
                    .version("2.15.1"),
            plugin -> Collections.singleton(plugin.getRelocation("de{}tr7zw{}changeme{}nbtapi"))),
    /**
     * {@code com.h2database:h2}
     */
    H2(
            () -> Library.builder()
                    .repository(Repositories.MAVEN_CENTRAL)
                    .groupId("com{}h2database")
                    .artifactId("h2")
                    .version("2.2.224"), // Don't update to keep support for Java 8
            plugin -> Collections.singleton(plugin.getRelocation("org{}h2"))),
    /**
     * {@code org.postgresql:postgresql}
     */
    POSTGRESQL(
            () -> Library.builder()
                    .repository(Repositories.MAVEN_CENTRAL)
                    .groupId("org{}postgresql")
                    .artifactId("postgresql")
                    .version("42.7.7"),
            plugin -> Collections.singleton(plugin.getRelocation("org{}postgresql")));

    /**
     * {@link AnnoyingLibrary#getLibrarySupplier()}
     */
    @NotNull public final Supplier<Library.Builder> librarySupplier;
    /**
     * {@link AnnoyingLibrary#getRelocations()}
     */
    @NotNull public final Function<AnnoyingPlugin, Collection<Relocation>> relocations;

    /**
     * Creates a new {@link RuntimeLibrary} with relocations
     *
     * @param   librarySupplier  {@link #librarySupplier}
     * @param   relocations     {@link #relocations}
     */
    RuntimeLibrary(@NotNull Supplier<Library.Builder> librarySupplier, @NotNull Function<AnnoyingPlugin, Collection<Relocation>> relocations) {
        this.librarySupplier = librarySupplier;
        this.relocations = relocations;
    }

    @Override @NotNull
    public Supplier<Library.Builder> getLibrarySupplier() {
        return librarySupplier;
    }

    @Override @NotNull
    public Function<AnnoyingPlugin, Collection<Relocation>> getRelocations() {
        return relocations;
    }
}
