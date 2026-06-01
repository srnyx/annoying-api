package xyz.srnyx.annoyingapi.stats.loader;

import dev.faststats.ErrorTracker;
import dev.faststats.FeatureFlagService;
import dev.faststats.Metrics;
import dev.faststats.bukkit.BukkitContext;
import dev.faststats.data.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.BuildProperties;

import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;


public abstract class FastStatsLoader extends StatsLoader<String, BukkitContext> {
    @NotNull public final ErrorTracker errorTracker = ErrorTracker.contextAware();

    @Nullable private BukkitContext apiStats;

    /**
     * Do not use {@link BukkitContext.Factory#metrics(Function)}, {@link BukkitContext.Factory#featureFlagService(Function)}, or other similar service creators <b>unless</b> you want to overwrite the default ones the API creates!
     */
    public void mutateContextFactory(@NotNull BukkitContext.Factory factory) {}

    public void mutateMetricsFactory(@NotNull Metrics.Factory factory) {}

    public void mutateFeatureFlagService(@NotNull FeatureFlagService.Factory factory) {}

    @Override
    public void load() {
        final AnnoyingPlugin plugin = getAnnoyingPlugin();
        final List<Metric<?>> commonMetrics = List.of(
                Metric.string("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION),
                Metric.string("storage_method", plugin.statsHelper::getStorageMethodName),
                Metric.bool("storage_cache_enabled", plugin.statsHelper::getStorageCacheEnabled),
                Metric.stringArray("storage_cache_save_on", plugin.statsHelper::getStorageCacheSaveOn),
                Metric.number("storage_cache_interval", plugin.statsHelper::getStorageCacheInterval),
                Metric.stringArray("messages_plugin_global_placeholders_keys", plugin.statsHelper::getMessagesPluginGlobalPlaceholdersKeys),
                Metric.string("messages_plugin_splitters_json", plugin.statsHelper::getMessagesPluginSplittersJson),
                Metric.string("messages_plugin_splitters_placeholder", plugin.statsHelper::getMessagesPluginSplittersPlaceholder),
                Metric.string("placeholderapi_version", plugin.statsHelper::getPlaceholderAPIVersion),
                Metric.string("update_checker_outdated_latest_version", plugin.statsHelper::getUpdateCheckerOutdatedLatestVersion));

        // API
        apiStats = new BukkitContext.Factory(plugin, "724dd679781f2a22c15aefa4b8a7bbcd")
                .errorTrackerService(errorTracker)
                .metrics(factory -> {
                    factory.addMetric(Metric.string("plugins", plugin::getName));
                    commonMetrics.forEach(factory::addMetric);
                    return factory.create();
                })
                .create();
        apiStats.ready();

        // Plugin
        final BukkitContext.Factory context = new BukkitContext.Factory(plugin, getId())
                .errorTrackerService(errorTracker)
                .metrics(factory -> {
                    commonMetrics.forEach(factory::addMetric);
                    mutateMetricsFactory(factory);
                    return factory.create();
                })
                .featureFlagService(factory -> {
                    mutateFeatureFlagService(factory);
                    return factory.create();
                });
        mutateContextFactory(context);
        stats = context.create();
        stats.ready();

        // Log
        AnnoyingPlugin.log(Level.INFO, "Loaded FastStats metrics");
    }

    @Override
    public void unload() {
        if (apiStats != null) apiStats.shutdown();
        if (stats != null) stats.shutdown();
    }
}
