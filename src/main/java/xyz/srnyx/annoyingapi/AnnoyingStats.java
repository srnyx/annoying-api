package xyz.srnyx.annoyingapi;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Wrapper for the {@link Metrics bStats} instance
 */
public class AnnoyingStats {
    /**
     * The {@link Metrics bStats} instance for the plugin
     */
    @Nullable public Metrics bStats;

    /**
     * Creates a new {@link AnnoyingStats} instance
     *
     * @param   plugin  the plugin to create the stats for
     */
    public AnnoyingStats(@NotNull AnnoyingPlugin plugin) {
        // API
        final Metrics api = new Metrics(plugin, 18281);
        api.addCustomChart(new SimplePie("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION));
        api.addCustomChart(new SimplePie("plugins", plugin::getName));
        api.addCustomChart(new SimplePie("storage_method", () -> plugin.dataManager == null ? "N/A" : plugin.dataManager.storageConfig.method.name()));

        // Plugin
        if (plugin.options.bStatsOptions.id != null) bStats = new Metrics(plugin, plugin.options.bStatsOptions.id);
    }
}
