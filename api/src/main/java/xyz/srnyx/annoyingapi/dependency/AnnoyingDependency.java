package xyz.srnyx.annoyingapi.dependency;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.PluginPlatform;
import xyz.srnyx.annoyingapi.utility.ConfigurationUtility;

import xyz.srnyx.javautilities.parents.Stringable;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Represents a downloadable dependency
 */
public class AnnoyingDependency extends Stringable {
    /**
     * The name of the dependency (from it's {@code plugin.yml})
     */
    @NotNull public final String name;
    /**
     * The platforms the dependency can be downloaded from
     */
    @NotNull public final PluginPlatform.Multi platforms;
    /**
     * Whether the dependency is required to be installed. If the download fails and this is true, the plugin will not enable
     * <p><i>This does <b>NOT</b> stop the dependency from being downloaded/installed</i>
     */
    public final boolean required;
    /**
     * Whether to attempt to enable the dependency after it has been downloaded
     */
    public final boolean enableAfterDownload;
    /**
     * The new file of the dependency ({@link #name}{@code .jar})
     */
    @NotNull public final File file;

    /**
     * Creates a new dependency instance
     *
     * @param   name                    {@link #name}
     * @param   platforms               {@link #platforms}
     * @param   required                {@link #required}
     * @param   enableAfterDownload     {@link #enableAfterDownload}
     */
    public AnnoyingDependency(@NotNull String name, @NotNull PluginPlatform.Multi platforms, boolean required, boolean enableAfterDownload) {
        this.name = name;
        this.platforms = platforms;
        this.required = required;
        this.enableAfterDownload = enableAfterDownload;
        this.file = new File(Bukkit.getUpdateFolderFile().getParentFile(), name + ".jar");
    }

    /**
     * Creates a new dependency instance
     *
     * @param   name                {@link #name}
     * @param   required            {@link #required}
     * @param   enableAfterDownload {@link #enableAfterDownload}
     * @param   platforms           {@link #platforms}
     */
    public AnnoyingDependency(@NotNull String name, boolean required, boolean enableAfterDownload, @NotNull PluginPlatform.Multi platforms) {
        this(name, platforms, required, enableAfterDownload);
    }

    /**
     * Creates a new dependency instance
     *
     * @param   name                {@link #name}
     * @param   required            {@link #required}
     * @param   enableAfterDownload {@link #enableAfterDownload}
     * @param   platforms           {@link #platforms}
     */
    public AnnoyingDependency(@NotNull String name, boolean required, boolean enableAfterDownload, @NotNull PluginPlatform... platforms) {
        this(name, new PluginPlatform.Multi(platforms), required, enableAfterDownload);
    }

    /**
     * Loads a dependency from a {@link ConfigurationSection}
     *
     * @param   section the section to load from
     *
     * @return          the loaded dependency
     */
    @NotNull
    public static AnnoyingDependency load(@NotNull ConfigurationSection section) {
        String name = section.getName();
        if (name.isEmpty()) {
            name = section.getString("name");
            if (name == null) throw new IllegalArgumentException("The name of the dependency is missing");
        }
        return new AnnoyingDependency(
                name,
                PluginPlatform.Multi.load(section, "platforms"),
                section.getBoolean("required"),
                section.getBoolean("enableAfterDownload"));
    }

    /**
     * Loads a list of dependencies from a {@link ConfigurationSection}
     *
     * @param   section the section to load from
     * @param   key     the key to load from
     *
     * @return          the loaded dependencies
     */
    @NotNull
    public static List<AnnoyingDependency> loadList(@NotNull ConfigurationSection section, @NotNull String key) {
        final ConfigurationSection dependenciesSection = section.getConfigurationSection(key);
        return (dependenciesSection == null ? ConfigurationUtility.toConfigurationList(section.getMapList(key)).stream() : dependenciesSection.getKeys(false).stream().map(dependenciesSection::getConfigurationSection).filter(Objects::nonNull))
                .map(AnnoyingDependency::load)
                .collect(Collectors.toList());
    }

    /**
     * This uses {@link Bukkit#getPluginManager()} to check if the dependency isn't installed. So it's vital that {@link #name} is from the plugin's {@code plugin.yml}
     *
     * @return  whether the dependency isn't currently installed
     */
    public boolean isNotInstalled() {
        return Bukkit.getPluginManager().getPlugin(name) == null;
    }
}
