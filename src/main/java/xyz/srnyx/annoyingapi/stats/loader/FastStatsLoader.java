package xyz.srnyx.annoyingapi.stats.loader;

import dev.faststats.ErrorTracker;
import dev.faststats.FeatureFlagService;
import dev.faststats.Metrics;
import dev.faststats.bukkit.BukkitContext;
import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.data.Metric;
import dev.faststats.data.SourceId;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.schema.FieldDeclaration;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.BuildProperties;
import xyz.srnyx.annoyingapi.stats.Stat;
import xyz.srnyx.annoyingapi.stats.Statable;
import xyz.srnyx.annoyingapi.storage.dialects.Dialect;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;


public abstract class FastStatsLoader extends StatsLoader<String, BukkitContext> {
    @NotNull public final ErrorTracker errorTracker = ErrorTracker.contextAware();

    @Nullable private BukkitContext apiStats;
    @Nullable private Dialect.Stats storageStats;

    @Nullable
    public Map<String, Supplier<OkaeriConfig>> getConfigs() {
        return null;
    }

    /**
     * Do not use {@link BukkitMetrics.Factory#onFlush(Runnable)} <b>unless</b> you want to overwrite the default one the API creates (may break some metrics)!
     */
    public void onFlush() {}

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
                // Config values that can't be in a map (arrays)
                enumArray("storage_cache_save_on", () -> plugin.dataManager == null ? null : plugin.dataManager.storageConfig.cache.getSaveOn()),
                stringArray("messages_plugin_global_placeholders_keys", () -> plugin.getAnnoyingMessages().plugin.global_placeholders.keySet()),
                // Misc
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
        commonMetrics.add(Metric.stringMap("storage", () -> plugin.dataManager == null ? null : processFields(plugin.dataManager.storageConfig)));

        // Common flush
        final Runnable defaultFlush = () -> storageStats = null;

        // API
        apiStats = new BukkitContext.Factory(plugin, "724dd679781f2a22c15aefa4b8a7bbcd")
                .errorTrackerService(errorTracker)
                .metrics(factory -> {
                    // Metrics
                    commonMetrics.forEach(factory::addMetric);
                    factory.addMetric(Metric.string("plugins", plugin::getName));
                    factory.addMetric(Metric.stringMap("messages", () -> processFields(plugin.getAnnoyingMessages())));

                    // Flush
                    factory.onFlush(defaultFlush);

                    return factory.create();
                })
                .create();
        apiStats.ready();

        // Plugin
        final BukkitContext.Factory context = new BukkitContext.Factory(plugin, getId())
                .errorTrackerService(errorTracker)
                .metrics(factory -> {
                    // messages
                    factory.addMetric(Metric.stringMap("messages", () -> processFields(plugin.getMessages().get())));

                    // Custom configs
                    final Map<String, Supplier<OkaeriConfig>> configs = getConfigs();
                    if (configs != null) for (final Map.Entry<String, Supplier<OkaeriConfig>> entry : configs.entrySet()) {
                        factory.addMetric(Metric.stringMap(entry.getKey(), () -> processFields(entry.getValue().get())));
                    }

                    // Metrics
                    commonMetrics.forEach(factory::addMetric);
                    mutateMetricsFactory(factory);

                    // Flushes
                    factory.onFlush(() -> {
                        defaultFlush.run(); // Common
                        onFlush(); // Custom
                    });

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
    private static Map<String, String> processFields(@NotNull OkaeriConfig config) {
        final Map<String, String> map = new LinkedHashMap<>();
        processFields("", map, config);
        return map;
    }

    private static void processFields(@NotNull String prefix, @NotNull Map<String, String> map, @NotNull OkaeriConfig config) {
        for (final FieldDeclaration field : config.getDeclaration().getFields()) {
            // Ignore transient
            if (Modifier.isTransient(field.getField().getModifiers())) continue;

            final Object value = field.getValue();
            if (value == null) continue;
            final String entryName = prefix + field.getName();

            // Config
            if (value instanceof OkaeriConfig subConfig) {
                processFields(entryName + "_", map, subConfig);
                continue;
            }

            // Check @Stat
            final Stat stat = field.getAnnotation(Stat.class).orElse(null);
            if (stat == null) continue;

            // Array/Collection/Map (partly supported)
            final boolean isArray = field.getType().getType().isArray();
            if (isArray || value instanceof Collection || value instanceof Map) {
                if (stat.size()) {
                    // Get size
                    final int size;
                    if (isArray) {
                        size = Array.getLength(value);
                    } else if (value instanceof Collection<?> collection) {
                        size = collection.size();
                    } else {
                        size = ((Map<?, ?>) value).size();
                    }

                    // Put size
                    map.put(entryName + "_size", String.valueOf(size));
                }
                continue;
            }

            // Statable
            if (value instanceof Statable statable) {
                final Map<String, Object> statMap = statable.toStatMap();
                if (statMap != null) {
                    // Map
                    for (final Map.Entry<String, Object> entry : statMap.entrySet()) {
                        final Object mapValue = entry.getValue();
                        if (mapValue != null) map.put(entryName + "_" + entry.getKey(), String.valueOf(mapValue));
                    }
                } else {
                    // Value
                    final Object statValue = statable.toStatValue();
                    if (statValue != null) map.put(entryName, String.valueOf(statValue));
                }
                continue;
            }

            // Duration
            if (value instanceof Duration duration) {
                map.put(entryName, String.valueOf(duration.toMillis()));
                continue;
            }

            // Else
            map.put(entryName, String.valueOf(value));
        }
    }
}
