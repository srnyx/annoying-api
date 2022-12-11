package xyz.srnyx.annoyingapi.dependency;

import org.bukkit.Bukkit;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;


/**
 * Represents a downloadable dependency
 */
public class AnnoyingDependency {
    @NotNull public final String name;
    @NotNull public final Map<AnnoyingDownload.Platform, String> platforms;
    public final boolean required;
    public final boolean enableAfterDownload;

    /**
     * Creates a new dependency instance
     *
     * @param   name                    the name of the dependency (from it's {@code plugin.yml})
     * @param   platforms               the platforms the dependency can be downloaded from
     * @param   required                whether the dependency is required to be installed
     * @param   enableAfterDownload     whether or not to attempt to enable the dependency after it has been downloaded
     */
    @Contract(pure = true)
    public AnnoyingDependency(@NotNull String name, @NotNull Map<AnnoyingDownload.Platform, String> platforms, boolean required, boolean enableAfterDownload) {
        this.name = name;
        this.platforms = platforms;
        this.required = required;
        this.enableAfterDownload = enableAfterDownload;
    }

    /**
     * Gets the new file of the dependency
     *
     * @return  the new file of the dependency ({@link #name}{@code .jar})
     */
    public File getFile() {
        return new File(Bukkit.getUpdateFolderFile().getParentFile(), name + ".jar");
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
