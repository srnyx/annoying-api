package xyz.srnyx.annoyingapi.storage.dialects;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.storage.DataManager;
import xyz.srnyx.annoyingapi.file.AnnoyingData;
import xyz.srnyx.annoyingapi.storage.FailedSet;
import xyz.srnyx.annoyingapi.storage.Value;

import xyz.srnyx.javautilities.FileUtility;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Data dialect for JSON database
 */
public class YAMLDialect extends Dialect {
    @NotNull private final Map<String, AnnoyingData> tables = new HashMap<>();

    /**
     * Creates a new H2 dialect
     *
     * @param   dataManager {@link #dataManager}
     */
    public YAMLDialect(@NotNull DataManager dataManager) {
        super(dataManager);
    }

    @NotNull
    private Optional<AnnoyingData> getTableFromCache(@NotNull String table) {
        return Optional.ofNullable(tables.get(table));
    }

    @Override @Nullable
    public Value getFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        return getTableFromCache(table).map(file -> new Value(file.getString(target + "." + key))).orElse(null);
    }

    @Override
    public void setToCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull Value value) {
        getTableFromCache(table)
                .orElseGet(() -> {
                    final AnnoyingData file = getTableFromDatabase(table);
                    tables.put(table, file);
                    return file;
                })
                .set(target + "." + key, value.value);
    }

    @Override
    public void markRemovedInCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        getTableFromCache(table).ifPresent(file -> file.set(target + "." + key, null));
    }

    @Override
    public void saveCacheImpl() {
        for (final AnnoyingData file : tables.values()) file.save();
    }

    @Override
    public void saveCacheImpl(@NotNull String table, @NotNull String target) {
        getTableFromCache(table).ifPresent(AnnoyingData::save);
    }

    @NotNull
    private AnnoyingData getTableFromDatabase(@NotNull String table) {
        return new AnnoyingData(dataManager.plugin, "yaml/" + table + ".yaml");
    }

    @Override @NotNull
    protected Optional<MigrationData> getMigrationDataFromDatabaseImpl(@NotNull DataManager newManager) {
        final Map<String, Set<String>> tablesKeys = new HashMap<>(); // [table, [column]]
        final Map<String, Map<String, ConcurrentHashMap<String, Value>>> data = new HashMap<>(); // [table, [target, [key, value]]]
        for (final String table : FileUtility.getFileNames(new File(dataManager.plugin.getDataFolder(), "data/yaml"), "yaml")) {
            final AnnoyingData file = getTableFromDatabase(table);
            final Set<String> keys = new HashSet<>();
            final Map<String, ConcurrentHashMap<String, Value>> tableData = new HashMap<>(); // [target, [key, value]]
            for (final String target : file.getKeys(false)) {
                final ConfigurationSection targetData = file.getConfigurationSection(target);
                if (targetData == null) continue;
                final ConcurrentHashMap<String, Value> targetMap = new ConcurrentHashMap<>(); // [key, value]
                for (final String key : targetData.getKeys(false)) {
                    keys.add(key);
                    targetMap.put(key, new Value(targetData.getString(key)));
                }
                tableData.put(target, targetMap);
            }
            tablesKeys.put(table, keys);
            data.put(table, tableData);
        }
        return Optional.of(new MigrationData(tablesKeys, data));
    }

    @Override @NotNull
    protected Optional<String> getFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        return Optional.ofNullable(getTableFromDatabase(table).getString(target + "." + key));
    }

    @Override @Nullable
    protected FailedSet setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value) {
        return getTableFromDatabase(table).setSave(target + "." + key, value) ? null : new FailedSet(table, target, key, value);
    }

    @Override @NotNull
    protected Set<FailedSet> setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull ConcurrentHashMap<String, Value> data) {
        final Set<ConcurrentHashMap.Entry<String, Value>> entrySet = data.entrySet();

        // Set data in file
        final AnnoyingData file = getTableFromDatabase(table);
        ConfigurationSection targetData = file.getConfigurationSection(target);
        if (targetData == null) targetData = file.createSection(target);
        for (final ConcurrentHashMap.Entry<String, Value> entry : entrySet) targetData.set(entry.getKey(), entry.getValue().value);

        // Return failures if saving fails
        final Set<FailedSet> failed = new HashSet<>();
        if (file.save()) return failed;
        for (final ConcurrentHashMap.Entry<String, Value> entry : entrySet) failed.add(new FailedSet(table, target, entry.getKey(), entry.getValue().value));
        return failed;
    }

    @Override
    protected boolean removeFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        return getTableFromDatabase(table).setSave(target + "." + key, null);
    }
}
