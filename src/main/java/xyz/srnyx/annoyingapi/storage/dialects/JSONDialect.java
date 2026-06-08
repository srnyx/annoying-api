package xyz.srnyx.annoyingapi.storage.dialects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.storage.DataManager;
import xyz.srnyx.annoyingapi.storage.FailedSet;
import xyz.srnyx.annoyingapi.storage.CachedValue;
import xyz.srnyx.javautilities.FileUtility;
import xyz.srnyx.javautilities.manipulation.Mapper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


/**
 * Data dialect for JSON database
 */
public class JSONDialect extends Dialect {
    @NotNull private static final Gson GSON = new Gson();

    // Need to use paths to ensure compatibility across operating systems
    @NotNull private final File folder = dataManager.plugin.getDataFolder().toPath().resolve("data/json").toFile();
    @NotNull private final Map<String, JsonFile> tables = new HashMap<>();

    /**
     * Creates a new H2 dialect
     *
     * @param   dataManager {@link #dataManager}
     */
    public JSONDialect(@NotNull DataManager dataManager) {
        super(dataManager);
    }

    @NotNull
    private Optional<JsonFile> getTableFromCache(@NotNull String table) {
        return Optional.ofNullable(tables.get(table));
    }

    @Override @Nullable
    public CachedValue getFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        return getTableFromCache(table).map(file -> file.get(target, key)).orElse(null);
    }

    @Override
    public void setToCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull CachedValue value) {
        getTableFromCache(table)
                .orElseGet(() -> {
                    final JsonFile newFile = getTableFromDatabase(table);
                    tables.put(table, newFile);
                    return newFile;
                })
                .set(target, key, value.value());
    }

    @Override
    public void markRemovedInCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        getTableFromCache(table).ifPresent(file -> file.remove(target, key));
    }

    @Override
    public void saveCacheImpl() {
        for (final JsonFile file : tables.values()) file.save();
    }

    @Override
    public void saveCacheImpl(@NotNull String table, @NotNull String target) {
        getTableFromCache(table).ifPresent(JsonFile::save);
    }

    @NotNull
    private JsonFile getTableFromDatabase(@NotNull String table) {
        final File file = new File(folder, table + ".json");
        JsonObject json = new JsonObject();

        // Read the file if it exists
        if (file.exists()) try (final FileReader fileReader = new FileReader(file)) {
            json = GSON.fromJson(fileReader, JsonObject.class);
        } catch (final IOException | JsonParseException e) {
            // Reading failed, log error
            dataManager.plugin.logErrorTrack(Level.SEVERE, "&cFailed to read file for table &4" + table, e);
        }

        // Return new file
        return new JsonFile(dataManager.plugin, file, json);
    }

    @Override @NotNull
    protected Optional<MigrationData> getMigrationDataFromDatabaseImpl(@NotNull DataManager newManager) {
        final Map<String, Set<String>> tablesKeys = new HashMap<>(); // [table, [column]]
        final ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>>> data = new ConcurrentHashMap<>(); // [table, [target, [key, value]]]
        for (final String table : FileUtility.getFileNames(folder, "json")) {
            final Set<String> keys = new HashSet<>();
            final ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>> targetData = new ConcurrentHashMap<>(); // [target, [key, value]]
            for (final Map.Entry<String, JsonElement> entry : getTableFromDatabase(table).json.entrySet()) {
                final JsonElement entryElement = entry.getValue();
                if (!entryElement.isJsonObject()) continue;
                final ConcurrentHashMap<String, CachedValue> targetMap = new ConcurrentHashMap<>(); // [key, value]
                for (final Map.Entry<String, JsonElement> targetEntry : entryElement.getAsJsonObject().entrySet()) {
                    final String key = targetEntry.getKey();
                    keys.add(key);
                    targetMap.put(key, new CachedValue(targetEntry.getValue().getAsString()));
                }
                targetData.put(entry.getKey(), targetMap);
            }
            tablesKeys.put(table, keys);
            data.put(table, targetData);
        }
        return Optional.of(new MigrationData(tablesKeys, data));
    }

    @Override @NotNull
    protected Optional<String> getFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        return Optional.ofNullable(getTableFromDatabase(table).get(target, key)).map(CachedValue::value);
    }

    @Override @Nullable
    protected FailedSet setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value) {
        final JsonFile file = getTableFromDatabase(table);
        file.set(target, key, value);
        return file.save() ? null : new FailedSet(table, target, key, value);
    }

    @Override @NotNull
    protected Set<FailedSet> setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull ConcurrentHashMap<String, CachedValue> data) {
        final Set<ConcurrentHashMap.Entry<String, CachedValue>> entrySet = data.entrySet();

        // Set data in file
        final JsonFile file = getTableFromDatabase(table);
        final JsonObject targetData = file.getTargetDataCreate(target);
        for (final ConcurrentHashMap.Entry<String, CachedValue> entry : entrySet) targetData.addProperty(entry.getKey(), entry.getValue().value());

        // Return failures if saving fails
        final Set<FailedSet> failed = new HashSet<>();
        if (file.save()) return failed;
        for (final ConcurrentHashMap.Entry<String, CachedValue> entry : entrySet) failed.add(new FailedSet(table, target, entry.getKey(), entry.getValue().value()));
        return failed;
    }

    @Override
    protected boolean removeFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        final JsonFile file = getTableFromDatabase(table);
        file.remove(target, key);
        return file.save();
    }

    /**
     * A wrapper for a JSON file with utility methods
     */
    public static class JsonFile {
        @NotNull private final AnnoyingPlugin plugin;
        /**
         * The file
         */
        @NotNull private final File file;
        /**
         * The JSON object containing the data
         */
        @NotNull public final JsonObject json;

        /**
         * Creates a new JSON file
         *
         * @param   file    {@link #file}
         * @param   json    {@link #json}
         */
        private JsonFile(@NotNull AnnoyingPlugin plugin, @NotNull File file, @NotNull JsonObject json) {
            this.plugin = plugin;
            this.file = file;
            this.json = json;
        }

        /**
         * Gets the target data
         *
         * @param   target  the target to get the data from
         *
         * @return          the data of the target, or {@link Optional#empty()} if the target doesn't exist
         */
        @NotNull
        private Optional<JsonObject> getTargetData(@NotNull String target) {
            return Mapper.convertJsonElement(json.get(target), JsonObject.class);
        }

        /**
         * Gets the target data
         *
         * @param   target  the target to get the data from
         *
         * @return          the data of the target, or {@link Optional#empty()} if the target doesn't exist
         */
        @NotNull
        private JsonObject getTargetDataCreate(@NotNull String target) {
            return getTargetData(target).orElseGet(() -> {
                final JsonObject jsonObject = new JsonObject();
                json.add(target, jsonObject);
                return jsonObject;
            });
        }

        /**
         * Gets a key's value from the target
         *
         * @param   target  the target to get the key's value from
         * @param   key     the key to get
         *
         * @return          the value of the key in a {@link CachedValue}, or null if the key doesn't exist
         */
        @Nullable
        private CachedValue get(@NotNull String target, @NotNull String key) {
            return getTargetData(target)
                    .flatMap(jsonObject -> Mapper.convertJsonElement(jsonObject.get(key), JsonPrimitive.class))
                    .flatMap(primitive -> Mapper.convertJsonPrimitive(primitive, String.class))
                    .map(CachedValue::new)
                    .orElse(null);
        }

        /**
         * Sets a key in the target
         *
         * @param   target  the target to set the key in
         * @param   key     the key to set
         * @param   value   the value to set
         */
        private void set(@NotNull String target, @NotNull String key, @Nullable String value) {
            getTargetDataCreate(target).addProperty(key, value);
        }

        /**
         * Removes a key from the target
         *
         * @param   target  the target to remove the key from
         * @param   key     the key to remove
         */
        private void remove(@NotNull String target, @NotNull String key) {
            getTargetData(target).ifPresent(jsonObject -> jsonObject.remove(key));
        }

        /**
         * Saves the file to disk
         *
         * @return  {@code true} if the file was saved successfully, {@code false} otherwise
         */
        private boolean save() {
            // Create file if it doesn't exist
            if (!file.exists()) try {
                Files.createDirectories(file.getParentFile().toPath());
                Files.createFile(file.toPath());
            } catch (final IOException e) {
                plugin.logErrorTrack(Level.SEVERE, "&cFailed to create file for table &4" + file.getName(), e);
                return false;
            }

            // Write data to file
            try (final FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(GSON.toJson(json));
                return true;
            } catch (final IOException e) {
                plugin.logErrorTrack(Level.SEVERE, "&cFailed to save file for table &4" + file.getName(), e);
                return false;
            }
        }
    }
}
