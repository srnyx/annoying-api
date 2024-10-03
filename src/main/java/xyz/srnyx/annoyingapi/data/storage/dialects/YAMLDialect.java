package xyz.srnyx.annoyingapi.data.storage.dialects;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.data.storage.DataManager;
import xyz.srnyx.annoyingapi.file.AnnoyingData;
import xyz.srnyx.annoyingapi.file.AnnoyingFile;

import xyz.srnyx.javautilities.FileUtility;

import java.io.File;
import java.util.*;


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

    @Override @NotNull
    public Optional<String > getFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        return getTableFromCache(table).map(file -> file.getString(target + "." + key));
    }

    @Override
    public void setToCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value) {
        getTableFromCache(table).ifPresent(file -> file.set(target + "." + key, value));
    }

    @Override
    public void removeFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
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
        // Save cache
        getTableFromCache(table).ifPresent(AnnoyingFile::save);
        // Add to cache
        final AnnoyingData newFile = new AnnoyingData(dataManager.plugin, "yaml/" + table + ".yaml");
        tables.put(table, newFile);
        // Return new file
        return newFile;
    }

    @Override @NotNull
    protected Optional<MigrationData> getMigrationDataFromDatabaseImpl(@NotNull DataManager newManager) {
        final Map<String, Set<String>> tablesKeys = new HashMap<>(); // [table, [column]]
        final Map<String, Map<String, Map<String, String>>> data = new HashMap<>(); // [table, [target, [key, value]]]
        for (final String table : FileUtility.getFileNames(new File(dataManager.plugin.getDataFolder(), "data/yaml"), "yaml")) {
            final AnnoyingData file = getTableFromDatabase(table);
            final Set<String> keys = new HashSet<>();
            final Map<String, Map<String, String>> tableData = new HashMap<>(); // [target, [key, value]]
            for (final String target : file.getKeys(false)) {
                final ConfigurationSection targetData = file.getConfigurationSection(target);
                if (targetData == null) continue;
                final Map<String, String> targetMap = new HashMap<>(); // [key, value]
                for (final String key : targetData.getKeys(false)) {
                    keys.add(key);
                    targetMap.put(key, targetData.getString(key));
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

    @Override
    protected boolean setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value) {
        return getTableFromDatabase(table).setSave(target + "." + key, value);
    }

    @Override
    protected boolean setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data) {
        final AnnoyingData file = getTableFromDatabase(table);
        ConfigurationSection targetData = file.getConfigurationSection(target);
        if (targetData == null) targetData = file.createSection(target);
        for (final Map.Entry<String, String> entry : data.entrySet()) targetData.set(entry.getKey(), entry.getValue());
        return file.save();
    }

    @Override
    protected boolean removeFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        return getTableFromDatabase(table).setSave(target + "." + key, null);
    }
}
