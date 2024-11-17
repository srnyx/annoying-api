package xyz.srnyx.annoyingapi.options;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.storage.DataManager;
import xyz.srnyx.annoyingapi.data.StringData;
import xyz.srnyx.annoyingapi.file.AnnoyingFile;
import xyz.srnyx.annoyingapi.data.EntityData;

import xyz.srnyx.javautilities.MapGenerator;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.*;
import java.util.function.Consumer;


/**
 * These options all relate to SQL data management utilities such as {@link StringData} and {@link EntityData}
 */
public class DataOptions extends Stringable {
    /**
     * Whether to enable {@link DataManager data management}
     * <br>You <b>need</b> to enable this if you're using {@link StringData} or {@link EntityData}!
     */
    public boolean enabled = false;
    /**
     * A set of all tables/columns to be created for {@link DataManager}. Do not include the table prefix!
     * <br>This is case-insensitive, so all keys and values will be converted to lowercase
     * <br><b>All tables will be made with the primary key {@code target}</b>
     * <br><i>If no custom columns are added to {@link EntityData#TABLE_NAME}, it won't be created. Manually removing it will break {@link EntityData}</i>
     */
    @NotNull @SuppressWarnings("CanBeFinal") public Map<String, Set<String>> tables = MapGenerator.HASH_MAP.mapOf(EntityData.TABLE_NAME, new HashSet<>(Collections.singleton(StringData.TARGET_COLUMN)));
    /**
     * Whether to use the cache by default for {@link StringData}
     */
    public boolean useCacheDefault = true;
    /**
     * Options for {@link EntityData entity data management}
     */
    @NotNull public Entities entities = new Entities();

    /**
     * Sets {@link #enabled}
     *
     * @param   enabled the new value
     *
     * @return          this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Adds all the specified tables to {@link #tables}
     * <br>All tables and columns will be converted to lowercase
     *
     * @param   tables  the tables to add
     *
     * @return          this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions tables(@NotNull Map<String, Collection<String>> tables) {
        this.tables.putAll(tables.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey().toLowerCase(), entry.getValue().stream()
                        .map(String::toLowerCase)
                        .collect(HashSet::new, HashSet::add, HashSet::addAll)), HashMap::putAll));
        return this;
    }

    /**
     * Adds the specified table to {@link #tables}
     * <br>The table and all columns will be converted to lowercase
     *
     * @param   table   the table to add
     * @param   columns the columns to add for the table
     *
     * @return          this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions table(@NotNull String table, @NotNull Collection<String> columns) {
        this.tables.put(table.toLowerCase(), columns.stream()
                .map(String::toLowerCase)
                .collect(HashSet::new, HashSet::add, HashSet::addAll));
        return this;
    }

    /**
     * Adds the specified table to {@link #tables}
     * <br>The table and all columns will be converted to lowercase
     *
     * @param   table   the table to add
     * @param   columns the columns to add for the table
     *
     * @return          this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions table(@NotNull String table, @NotNull String... columns) {
        return table(table, Arrays.asList(columns));
    }

    /**
     * Adds the specified columns to the {@link EntityData#TABLE_NAME} table
     *
     * @param   columns the columns to add
     *
     * @return          this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions entityDataColumns(@NotNull Collection<String> columns) {
        final Set<String> entityDataColumns = tables.get(EntityData.TABLE_NAME);
        if (entityDataColumns == null) return table(EntityData.TABLE_NAME, new HashSet<>(columns));
        entityDataColumns.addAll(columns);
        return this;
    }

    /**
     * Adds the specified columns to the {@link EntityData#TABLE_NAME} table
     *
     * @param   columns the columns to add
     *
     * @return          this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions entityDataColumns(@NotNull String... columns) {
        return entityDataColumns(Arrays.asList(columns));
    }

    /**
     * Sets {@link #useCacheDefault}
     *
     * @param   useCacheDefault the new value
     *
     * @return                  this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions useCacheDefault(boolean useCacheDefault) {
        this.useCacheDefault = useCacheDefault;
        return this;
    }

    /**
     * Sets {@link #entities}
     *
     * @param   entities    the new value
     *
     * @return              this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions entities(@NotNull Entities entities) {
        this.entities = entities;
        return this;
    }

    /**
     * Sets {@link #entities} using the specified {@link Consumer}
     *
     * @param   consumer    the consumer to accept the {@link Entities} instance
     *
     * @return              this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions entities(@NotNull Consumer<Entities> consumer) {
        consumer.accept(entities);
        return this;
    }

    /**
     * Constructs a new {@link DataOptions} instance with default values
     */
    public DataOptions() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * Loads the options from the specified {@link ConfigurationSection}
     *
     * @param   section the section to load the options from
     *
     * @return          the loaded options
     */
    @NotNull
    public static DataOptions load(@NotNull ConfigurationSection section) {
        final DataOptions options = new DataOptions();
        if (section.contains("enabled")) options.enabled(section.getBoolean("enabled"));
        final ConfigurationSection tablesSection = section.getConfigurationSection("tables");
        if (tablesSection != null) options.tables(tablesSection.getKeys(false).stream()
                .collect(HashMap::new, (map, table) -> map.put(table, tablesSection.getStringList(table)), HashMap::putAll));
        if (section.contains("useCacheDefault")) options.useCacheDefault(section.getBoolean("useCacheDefault"));
        final ConfigurationSection entitiesSection = section.getConfigurationSection("entities");
        if (entitiesSection != null) options.entities(Entities.load(entitiesSection));
        return options;
    }

    /**
     * Options for {@link EntityData entity data management}
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static class Entities extends Stringable {
        /**
         * The path to the folder (inside {@code plugins/PLUGIN/data/}) where the entity data files will be stored
         *
         * @deprecated  PDC/YML storage has been replaced with SQL storage
         */
        @NotNull @Deprecated public String path = "entities";
        /**
         * The options for all entity data files
         *
         * @deprecated  PDC/YML storage has been replaced with SQL storage
         */
        @Nullable @Deprecated public AnnoyingFile.Options<?> fileOptions = new AnnoyingFile.Options<>().canBeEmpty(false);
        /**
         * The YML section/node where the entity data will be stored
         *
         * @deprecated  PDC/YML storage has been replaced with SQL storage
         */
        @NotNull @Deprecated public String section = "data";

        /**
         * Constructs a new {@link Entities} instance with default values
         */
        public Entities() {
            // Only exists to give the constructor a Javadoc
        }

        /**
         * Loads the options from the specified {@link ConfigurationSection}
         *
         * @param   section the section to load the options from
         *
         * @return          the loaded options
         */
        @NotNull
        public static Entities load(@NotNull ConfigurationSection section) {
            final Entities options = new Entities();
            final String pathString = section.getString("path");
            if (pathString != null) options.path(pathString);
            final ConfigurationSection fileOptionsSection = section.getConfigurationSection("fileOptions");
            if (fileOptionsSection != null) options.fileOptions(AnnoyingFile.Options.load(fileOptionsSection));
            final String sectionString = section.getString("section");
            if (sectionString != null) options.node(sectionString);
            return options;
        }

        /**
         * Sets {@link #path}
         *
         * @param   path    the new value
         *
         * @return          this {@link Entities} instance for chaining
         *
         * @deprecated      PDC/YML storage has been replaced with SQL storage
         */
        @NotNull @Deprecated
        public Entities path(@NotNull String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets {@link #fileOptions}
         *
         * @param   fileOptions the new value
         *
         * @return              this {@link Entities} instance for chaining
         *
         * @deprecated          PDC/YML storage has been replaced with SQL storage
         */
        @NotNull @Deprecated
        public Entities fileOptions(@Nullable AnnoyingFile.Options<?> fileOptions) {
            this.fileOptions = fileOptions;
            return this;
        }

        /**
         * Sets {@link #fileOptions} using the specified {@link Consumer}
         *
         * @param   consumer    the {@link Consumer} to accept the new {@link #fileOptions}
         *
         * @return              this {@link Entities} instance for chaining
         *
         * @deprecated          PDC/YML storage has been replaced with SQL storage
         */
        @NotNull @Deprecated
        public Entities fileOptions(@NotNull Consumer<AnnoyingFile.Options<?>> consumer) {
            final AnnoyingFile.Options<?> options = new AnnoyingFile.Options<>();
            consumer.accept(options);
            return fileOptions(options);
        }

        /**
         * Sets {@link #section}
         *
         * @param   node    the new value
         *
         * @return          this {@link Entities} instance for chaining
         *
         * @deprecated      PDC/YML storage has been replaced with SQL storage
         */
        @NotNull @Deprecated
        public Entities node(@NotNull String node) {
            this.section = node;
            return this;
        }
    }
}
