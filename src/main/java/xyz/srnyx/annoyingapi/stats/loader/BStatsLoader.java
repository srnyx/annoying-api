package xyz.srnyx.annoyingapi.stats.loader;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.BuildProperties;

import java.util.concurrent.Callable;
import java.util.logging.Level;


public abstract class BStatsLoader extends StatsLoader<Integer, Metrics> {
    @Override
    public void load() {
        final AnnoyingPlugin plugin = getAnnoyingPlugin();
        final Callable<String> storageMethodName = () -> plugin.dataManager == null ? null : plugin.dataManager.storageConfig.method.name();

        // API
        final Metrics api = new Metrics(plugin, 18281);
        api.addCustomChart(new SimplePie("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION));
        api.addCustomChart(new SimplePie("plugins", plugin::getName));
        api.addCustomChart(new SimplePie("storage_method", storageMethodName));

        // Plugin
        stats = new Metrics(plugin, getId());
        stats.addCustomChart(new SimplePie("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION));
        stats.addCustomChart(new SimplePie("storage_method", storageMethodName));

        // Log
        AnnoyingPlugin.log(Level.INFO, "Loaded BStats metrics");
    }
}
