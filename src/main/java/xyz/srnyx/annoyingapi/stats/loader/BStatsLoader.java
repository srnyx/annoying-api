package xyz.srnyx.annoyingapi.stats.loader;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.BuildProperties;

import java.util.logging.Level;


public abstract class BStatsLoader extends StatsLoader<Integer, Metrics> {
    @Override
    public void load() {
        final AnnoyingPlugin plugin = getAnnoyingPlugin();

        // API
        final Metrics api = new Metrics(plugin, 18281);
        api.addCustomChart(new SimplePie("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION));
        api.addCustomChart(new SimplePie("plugins", plugin::getName));
        api.addCustomChart(new SimplePie("storage_method", plugin.statsHelper::getStorageMethodName));

        // Plugin
        stats = new Metrics(plugin, getId());
        stats.addCustomChart(new SimplePie("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION));
        stats.addCustomChart(new SimplePie("storage_method", plugin.statsHelper::getStorageMethodName));

        // Log
        AnnoyingPlugin.log(Level.INFO, "Loaded BStats metrics");
    }
}
