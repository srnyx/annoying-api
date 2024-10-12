package xyz.srnyx.annoyingapi.data.storage.dialects;

import com.google.gson.*;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.data.storage.DataManager;

import xyz.srnyx.javautilities.FileUtility;
import xyz.srnyx.javautilities.MiscUtility;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;


/**
 * Data dialect for JSON database
 */
public class JSONDialect extends Dialect {
    @NotNull private static final Gson GSON = new Gson();

    @NotNull private final File folder = new File(dataManager.plugin.getDataFolder(), "data/json");
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

    @Override @NotNull
    public Optional<String> getFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        return getTableFromCache(table).flatMap(file -> file.get(target, key));
    }

    @Override
    public void setToCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value) {
        getTableFromCache(table)
                .orElseGet(() -> {
                    final JsonFile newFile = getTableFromDatabase(table);
                    tables.put(table, newFile);
                    return newFile;
                })
                .set(target, key, value);
    }

    @Override
    public void removeFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
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
        // Reading failed, log error
        } catch (final IOException | JsonParseException e) {
            AnnoyingPlugin.log(Level.SEVERE, "&cFailed to read file for table &4" + table, e);
        }

        // Return new file
        return new JsonFile(file, json);
    }

    @Override @NotNull
    protected Optional<MigrationData> getMigrationDataFromDatabaseImpl(@NotNull DataManager newManager) {
        final Map<String, Set<String>> tablesKeys = new HashMap<>(); // [table, [column]]
        final Map<String, Map<String, Map<String, String>>> data = new HashMap<>(); // [table, [target, [key, value]]]
        for (final String table : FileUtility.getFileNames(folder, "json")) {
            final Set<String> keys = new HashSet<>();
            final Map<String, Map<String, String>> targetData = new HashMap<>(); // [target, [key, value]]
            for (final Map.Entry<String, JsonElement> entry : getTableFromDatabase(table).json.entrySet()) {
                final JsonElement entryElement = entry.getValue();
                if (!entryElement.isJsonObject()) continue;
                final Map<String, String> targetMap = new HashMap<>(); // [key, value]
                for (final Map.Entry<String, JsonElement> targetEntry : entryElement.getAsJsonObject().entrySet()) {
                    final String key = targetEntry.getKey();
                    keys.add(key);
                    targetMap.put(key, targetEntry.getValue().getAsString());
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
        return getTableFromDatabase(table).get(target, key);
    }

    @Override
    protected boolean setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value) {
        final JsonFile file = getTableFromDatabase(table);
        file.set(target, key, value);
        return file.save();
    }

    @Override
    protected boolean setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data) {
        final JsonFile file = getTableFromDatabase(table);
        final JsonObject targetData = file.getTargetDataCreate(target);
        for (final Map.Entry<String, String> entry : data.entrySet()) targetData.addProperty(entry.getKey(), entry.getValue());
        return file.save();
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
        private JsonFile(@NotNull File file, @NotNull JsonObject json) {
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
            return MiscUtility.handleException(() -> json.getAsJsonObject(target));
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
            return MiscUtility.handleException(() -> json.getAsJsonObject(target)).orElseGet(() -> {
                final JsonObject jsonObject = new JsonObject();
                json.add(target, jsonObject);
                return jsonObject;
            });
        }

        /**
         * Gets a key from the target
         *
         * @param   target  the target to get the key from
         * @param   key     the key to get
         *
         * @return          the value of the key, or {@link Optional#empty()} if the key doesn't exist
         */
        @NotNull
        private Optional<String> get(@NotNull String target, @NotNull String key) {
            return getTargetData(target).flatMap(jsonObject -> MiscUtility.handleException(() -> jsonObject.get(key).getAsString()));
        }

        /**
         * Sets a key in the target
         *
         * @param   target  the target to set the key in
         * @param   key     the key to set
         * @param   value   the value to set
         */
        private void set(@NotNull String target, @NotNull String key, @NotNull String value) {
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
                AnnoyingPlugin.log(Level.SEVERE, "&cFailed to create file for table &4" + file.getName(), e);
                return false;
            }

            // Write data to file
            try (final FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(GSON.toJson(json));
                return true;
            } catch (final IOException e) {
                AnnoyingPlugin.log(Level.SEVERE, "&cFailed to save file for table &4" + file.getName(), e);
                return false;
            }
        }
    }
}
