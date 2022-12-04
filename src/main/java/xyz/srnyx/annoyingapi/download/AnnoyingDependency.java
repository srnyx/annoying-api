package xyz.srnyx.annoyingapi.download;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;


/**
 * Represents a downloadable dependency
 *
 * @param   name        the name of the dependency (from it's {@code plugin.yml})
 * @param   platforms   the platforms the dependency can be downloaded from
 */
public record AnnoyingDependency(@NotNull String name, @NotNull Map<AnnoyingPlatform, String> platforms) {
    /**
     * This uses {@link Bukkit#getPluginManager()} to check if the dependency is installed. So it's vital that {@link #name} is from the plugin's {@code plugin.yml}
     *
     * @return  whether the dependency is currently installed
     */
    public boolean isInstalled() {
        return Bukkit.getPluginManager().getPlugin(name) != null || new File(Bukkit.getUpdateFolderFile().getParentFile(), name + ".jar").exists();
    }
}
