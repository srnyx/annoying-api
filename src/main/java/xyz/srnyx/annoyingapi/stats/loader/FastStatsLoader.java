package xyz.srnyx.annoyingapi.stats.loader;

import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import dev.faststats.core.data.Metric;

import me.clip.placeholderapi.PlaceholderAPIPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.BuildProperties;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;


public abstract class FastStatsLoader extends StatsLoader<String, BukkitMetrics> {
    @NotNull public final ErrorTracker errorTracker = ErrorTracker.contextAware();

    @Nullable private BukkitMetrics apiStats;

    @Nullable
    public Consumer<BukkitMetrics.Factory> getFactoryConsumer() {
        return null;
    }

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
                Metric.string("placeholderapi_version", () -> plugin.papiInstalled ? PlaceholderAPIPlugin.getInstance().getDescription().getVersion() : null),
                Metric.string("update_checker_outdated_latest_version", () -> plugin.updateChecker != null && plugin.updateChecker.latestVersion != null && plugin.updateChecker.isUpdateAvailable() ? plugin.updateChecker.latestVersion.toString() : null));

        // API
        final BukkitMetrics.Factory apiFactory = BukkitMetrics.factory()
                .token("724dd679781f2a22c15aefa4b8a7bbcd")
                .addMetric(Metric.string("plugins", plugin::getName));
        commonMetrics.forEach(apiFactory::addMetric);
        apiStats = apiFactory
                .errorTracker(errorTracker)
                .create(plugin);
        apiStats.ready();

        // Plugin
        final BukkitMetrics.Factory pluginFactory = BukkitMetrics.factory().token(getId());
        commonMetrics.forEach(pluginFactory::addMetric);
        final Consumer<BukkitMetrics.Factory> factoryConsumer = getFactoryConsumer();
        if (factoryConsumer != null) factoryConsumer.accept(pluginFactory);
        stats = pluginFactory
                .errorTracker(errorTracker)
                .create(plugin);
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
