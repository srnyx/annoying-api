package xyz.srnyx.annoyingapi.download;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


/**
 * Represents a downloadable dependency
 *
 * @param   name        the name of the dependency (from it's {@code plugin.yml})
 * @param   platforms   the platforms the dependency can be downloaded from
 */
public record AnnoyingDependency(@NotNull String name, @NotNull Map<AnnoyingPlatform, String> platforms) {
    /**
     * @return  whether the dependency is currently installed
     */
    public boolean isInstalled() {
        return Bukkit.getPluginManager().getPlugin(name) != null;
    }
}
