package xyz.srnyx.annoyingapi.stats.loader;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.BuildProperties;
import xyz.srnyx.annoyingapi.stats.provider.BStatsProvider;


/**
 * Wrapper for the {@link Metrics bStats} instance
 */
public class BStatsLoader extends StatsLoader<BStatsProvider, Metrics> {
    @Override
    public void load(@NotNull AnnoyingPlugin plugin, @NotNull BStatsProvider provider) {
        // API
        final Metrics api = new Metrics(plugin, 18281);
        api.addCustomChart(new SimplePie("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION));
        api.addCustomChart(new SimplePie("plugins", plugin::getName));
        api.addCustomChart(new SimplePie("storage_method", plugin.statsHelper::getStorageMethodName));

        // Plugin
        stats = new Metrics(plugin, provider.id);
        stats.addCustomChart(new SimplePie("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION));
        stats.addCustomChart(new SimplePie("storage_method", plugin.statsHelper::getStorageMethodName));
    }
}
