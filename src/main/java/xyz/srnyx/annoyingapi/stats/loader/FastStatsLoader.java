package xyz.srnyx.annoyingapi.stats.loader;

import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import dev.faststats.core.data.Metric;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.BuildProperties;
import xyz.srnyx.annoyingapi.stats.provider.FastStatsProvider;

import java.util.List;


public class FastStatsLoader extends StatsLoader<FastStatsProvider, BukkitMetrics> {
    @NotNull public final ErrorTracker apiErrorTracker = ErrorTracker.contextUnaware();
    @NotNull public final ErrorTracker pluginErrorTracker = ErrorTracker.contextAware();

    @Override
    public void load(@NotNull AnnoyingPlugin plugin, @NotNull FastStatsProvider provider) {
        final List<Metric<?>> commonMetrics = List.of(
                Metric.string("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION),
                Metric.string("storage_method", plugin.statsHelper::getStorageMethodName),
                Metric.bool("storage_cache_enabled", plugin.statsHelper::getStorageCacheEnabled),
                Metric.stringArray("storage_cache_save_on", plugin.statsHelper::getStorageCacheSaveOn),
                Metric.number("storage_cache_interval", plugin.statsHelper::getStorageCacheInterval),
                Metric.bool("papi_installed", () -> plugin.papiInstalled));

        // API
        final BukkitMetrics.Factory apiFactory = BukkitMetrics.factory()
                .token("724dd679781f2a22c15aefa4b8a7bbcd")
                .addMetric(Metric.string("plugins", plugin::getName));
        commonMetrics.forEach(apiFactory::addMetric);
        apiFactory
                .errorTracker(apiErrorTracker)
                .create(plugin)
                .ready();

        // Plugin TODO cant define plugin-specific stats. need to expose bukkitmetrics.factory or accept metrics here somehow. cant put in provider.
        final BukkitMetrics.Factory pluginFactory = BukkitMetrics.factory().token(provider.id);
        commonMetrics.forEach(pluginFactory::addMetric);
        stats = pluginFactory
                .errorTracker(pluginErrorTracker)
                .create(plugin);
        stats.ready();
    }

    @Override
    public void unload() {
        if (stats != null) stats.shutdown();
    }
}
