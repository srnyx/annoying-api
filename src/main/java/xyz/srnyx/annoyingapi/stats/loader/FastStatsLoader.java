package xyz.srnyx.annoyingapi.stats.loader;

import com.google.gson.JsonObject;
import dev.faststats.ErrorTracker;
import dev.faststats.FeatureFlagService;
import dev.faststats.Metrics;
import dev.faststats.bukkit.BukkitContext;
import dev.faststats.data.Metric;
import dev.faststats.data.SourceId;
import eu.okaeri.configs.OkaeriConfig;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.BuildProperties;
import xyz.srnyx.annoyingapi.stats.gson.StatsGson;
import xyz.srnyx.annoyingapi.storage.dialects.Dialect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Level;


public abstract class FastStatsLoader extends StatsLoader<String, BukkitContext> {
    @NotNull public final ErrorTracker errorTracker = ErrorTracker.contextAware();

    @Nullable private BukkitContext apiStats;
    @Nullable private Dialect.Stats storageStats;

    /**
     * Do not use {@link BukkitContext.Factory#metrics(Function)}, {@link BukkitContext.Factory#featureFlagService(Function)}, or other similar service creators <b>unless</b> you want to overwrite the default ones the API creates!
     */
    public void mutateContextFactory(@NotNull BukkitContext.Factory factory) {}

    public void mutateMetricsFactory(@NotNull Metrics.Factory factory) {}

    public void mutateFeatureFlagService(@NotNull FeatureFlagService.Factory factory) {}

    @Override
    public void load() {
        final AnnoyingPlugin plugin = getAnnoyingPlugin();

        // Common metrics
        final List<Metric<?>> commonMetrics = new ArrayList<>(List.of(
                Metric.string("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION),
                Metric.string("placeholderapi_version", () -> plugin.papiInstalled ? PlaceholderAPIPlugin.getInstance().getDescription().getVersion() : null),
                Metric.string("update_checker_outdated_latest_version", () ->
                        plugin.updateChecker != null && plugin.updateChecker.latestVersion != null && plugin.updateChecker.isUpdateAvailable()
                                ? plugin.updateChecker.latestVersion.version().version
                                : null),
                Metric.number("storage_cache_targets", () -> getStorageStats()
                        .map(Dialect.Stats::cacheTargets)
                        .orElse(null)),
                Metric.number("storage_cache_values", () -> getStorageStats()
                        .map(Dialect.Stats::cacheValues)
                        .orElse(null))));
        // Storage
        commonMetrics.add(config("storage", () -> plugin.dataManager == null ? null : plugin.dataManager.storageConfig));

        // Common flush
        final Runnable commonFlush = () -> storageStats = null;

        // API
        apiStats = new BukkitContext.Factory(plugin, "724dd679781f2a22c15aefa4b8a7bbcd")
                .errorTrackerService(errorTracker)
                .metrics(factory -> {
                    // Flush
                    factory.onFlush(commonFlush);

                    // Metrics
                    commonMetrics.forEach(factory::addMetric);
                    factory.addMetric(Metric.string("plugins", plugin::getName));
                    factory.addMetric(config("messages", plugin::getAnnoyingMessages));

                    return factory.create();
                })
                .create();
        apiStats.ready();

        // Plugin
        final BukkitContext.Factory context = new BukkitContext.Factory(plugin, getId())
                .errorTrackerService(errorTracker)
                .metrics(factory -> {
                    // Common flush
                    factory.onFlush(commonFlush);

                    // Metrics
                    commonMetrics.forEach(factory::addMetric);
                    factory.addMetric(config("messages", () -> plugin.getMessages().get()));
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

    @NotNull
    private Optional<Dialect.Stats> getStorageStats() {
        if (storageStats == null) storageStats = getAnnoyingPlugin().dataManager != null ? getAnnoyingPlugin().dataManager.dialect.getStats() : null;
        return Optional.ofNullable(storageStats);
    }

    @NotNull
    public static Metric<String[]> stringArray(@NotNull @SourceId String id, @NotNull Callable<@Nullable Collection<String>> callable) {
        return Metric.stringArray(id, () -> {
            final Collection<String> collection = callable.call();
            return collection == null ? null : collection.toArray(new String[0]);
        });
    }

    @NotNull
    public static Metric<String[]> enumArray(@NotNull @SourceId String id, @NotNull Callable<@Nullable Collection<? extends Enum<?>>> callable) {
        return Metric.stringArray(id, () -> {
            final Collection<? extends Enum<?>> collection = callable.call();
            return collection == null ? null : collection.stream()
                    .map(Enum::name)
                    .toArray(String[]::new);
        });
    }

    @NotNull
    public static Metric<Boolean[]> booleanArray(@NotNull @SourceId String id, @NotNull Callable<@Nullable Collection<Boolean>> callable) {
        return Metric.booleanArray(id, () -> {
            final Collection<Boolean> collection = callable.call();
            return collection == null ? null : collection.toArray(new Boolean[0]);
        });
    }

    @NotNull
    public static Metric<Number[]> numberArray(@NotNull @SourceId String id, @NotNull Callable<@Nullable Collection<Number>> callable) {
        return Metric.numberArray(id, () -> {
            final Collection<Number> collection = callable.call();
            return collection == null ? null : collection.toArray(new Number[0]);
        });
    }

    @NotNull
    public static Metric<JsonObject> config(@NotNull @SourceId String id, @NotNull Callable<@Nullable OkaeriConfig> callable) {
        return Metric.object(id, () -> {
            final OkaeriConfig config = callable.call();
            return config == null ? null : StatsGson.GSON.toJsonTree(config, OkaeriConfig.class).getAsJsonObject();
        });
    }
}
