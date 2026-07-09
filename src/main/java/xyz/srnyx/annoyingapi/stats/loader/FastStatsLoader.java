package xyz.srnyx.annoyingapi.stats.loader;

import dev.faststats.ErrorTracker;
import dev.faststats.FeatureFlagService;
import dev.faststats.Metrics;
import dev.faststats.bukkit.BukkitContext;
import dev.faststats.data.Metric;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.schema.FieldDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.BuildProperties;
import xyz.srnyx.annoyingapi.stats.Stat;
import xyz.srnyx.annoyingapi.stats.Statable;

import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;


public abstract class FastStatsLoader extends StatsLoader<String, BukkitContext> {
    @NotNull public final ErrorTracker errorTracker = ErrorTracker.contextAware();

    @Nullable private BukkitContext apiStats;

    @Nullable
    public Map<String, Supplier<OkaeriConfig>> getConfigs() {
        return null;
    }

    /**
     * Do not use {@link BukkitContext.Factory#metrics(Function)}, {@link BukkitContext.Factory#featureFlagService(Function)}, or other similar service creators <b>unless</b> you want to overwrite the default ones the API creates!
     */
    public void mutateContextFactory(@NotNull BukkitContext.Factory factory) {}

    public void mutateMetricsFactory(@NotNull Metrics.Factory factory) {}

    public void mutateFeatureFlagService(@NotNull FeatureFlagService.Factory factory) {}

    @Override
    public void load() {
        final AnnoyingPlugin plugin = getAnnoyingPlugin();
        final List<Metric<?>> commonMetrics = new ArrayList<>(List.of(
                Metric.string("annoying_api_version", () -> BuildProperties.ANNOYING_API_VERSION),
                Metric.string("placeholderapi_version", plugin.statsHelper::getPlaceholderAPIVersion),
                Metric.string("update_checker_outdated_latest_version", plugin.statsHelper::getUpdateCheckerOutdatedLatestVersion),
                Metric.stringArray("storage_cache_save_on", plugin.statsHelper::getStorageCacheSaveOn),
                Metric.stringArray("messages_plugin_global_placeholders_keys", plugin.statsHelper::getMessagesPluginGlobalPlaceholdersKeys)));

        // Storage
        commonMetrics.add(Metric.stringMap("storage", () -> plugin.dataManager == null ? null : processFields(plugin.dataManager.storageConfig)));

        // API
        apiStats = new BukkitContext.Factory(plugin, "724dd679781f2a22c15aefa4b8a7bbcd")
                .errorTrackerService(errorTracker)
                .metrics(factory -> {
                    factory.addMetric(Metric.string("plugins", plugin::getName));
                    factory.addMetric(Metric.stringMap("messages", () -> processFields(plugin.getAnnoyingMessages())));

                    commonMetrics.forEach(factory::addMetric);
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
            if (field.getAnnotation(Stat.class).isEmpty()) continue;

            // Collection/array (unsupported)
            final Class<?> rawType = field.getType().getType();
            if (rawType.isArray() || Collection.class.isAssignableFrom(rawType)) continue;

            // Statable
            if (value instanceof Statable statable) {
                final Map<String, Object> statMap = statable.toStatMap();
                if (statMap != null) {
                    // Map
                    for (final Map.Entry<String, Object> entry : statMap.entrySet()) {
                        final Object stat = entry.getValue();
                        if (stat != null) map.put(entryName + "_" + entry.getKey(), String.valueOf(stat));
                    }
                } else {
                    // Value
                    final Object stat = statable.toStatValue();
                    if (stat != null) map.put(entryName, String.valueOf(stat));
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
