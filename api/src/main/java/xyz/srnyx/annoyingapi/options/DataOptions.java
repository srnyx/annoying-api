package xyz.srnyx.annoyingapi.options;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.data.DataManager;
import xyz.srnyx.annoyingapi.data.StringData;
import xyz.srnyx.annoyingapi.file.AnnoyingFile;
import xyz.srnyx.annoyingapi.data.EntityData;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import xyz.srnyx.javautilities.MapUtility;
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
     * <br><b>All tables will be made with the primary key {@code target}</b>
     * <br><i>Removing {@link EntityData#TABLE_NAME} will break {@link EntityData}</i>
     */
    @NotNull public Map<String, Set<String>> tables = new HashMap<>(MapUtility.mapOf(EntityData.TABLE_NAME, new HashSet<>(Collections.singleton(StringData.TARGET_COLUMN))));
    /**
     * The options for the storage configuration file
     */
    @NotNull public ConfigFile configFile = new ConfigFile();
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
     *
     * @param   tables  the tables to add
     *
     * @return          this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions tables(@NotNull Map<String, Set<String>> tables) {
        this.tables.putAll(tables);
        return this;
    }

    /**
     * Adds the specified table to {@link #tables}
     *
     * @param   table   the table to add
     * @param   columns the columns to add for the table
     *
     * @return          this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions table(@NotNull String table, @NotNull Set<String> columns) {
        this.tables.put(table, columns);
        return this;
    }

    /**
     * Adds the specified table to {@link #tables}
     *
     * @param   table   the table to add
     * @param   columns the columns to add for the table
     *
     * @return          this {@link DataOptions} instance for chaining
     */
    @NotNull
    public DataOptions table(@NotNull String table, @NotNull String... columns) {
        return table(table, new HashSet<>(Arrays.asList(columns)));
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
     * Sets {@link #configFile}
     *
     * @param   configFile  the new value
     *
     * @return              this {@link Entities} instance for chaining
     */
    @NotNull
    public DataOptions configFile(@NotNull DataOptions.ConfigFile configFile) {
        this.configFile = configFile;
        return this;
    }

    /**
     * Sets {@link #configFile} using the specified {@link Consumer}
     *
     * @param   consumer    the {@link Consumer} to accept the new {@link #configFile}
     *
     * @return              this {@link Entities} instance for chaining
     */
    @NotNull
    public DataOptions configFile(@NotNull Consumer<ConfigFile> consumer) {
        final ConfigFile options = new ConfigFile();
        consumer.accept(options);
        return configFile(options);
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
        if (section.contains("configFile")) options.configFile(ConfigFile.load(section.getConfigurationSection("configFile")));
        if (section.contains("tables")) {
            final Map<String, Set<String>> tables = new HashMap<>();
            section.getConfigurationSection("tables").getKeys(false).forEach(table -> tables.put(table, new HashSet<>(section.getStringList("tables." + table))));
            options.tables(tables);
        }
        if (section.contains("entities")) options.entities(Entities.load(section.getConfigurationSection("entities")));
        return options;
    }

    /**
     * Options for {@link EntityData entity data management}
     */
    public static class Entities {
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
            if (section.contains("path")) options.path(section.getString("path"));
            if (section.contains("fileOptions")) options.fileOptions(AnnoyingFile.Options.load(section.getConfigurationSection("fileOptions")));
            if (section.contains("section")) options.node(section.getString("section"));
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

    /**
     * Options for the storage configuration file for {@link EntityData entity data}
     */
    public static class ConfigFile {
        /**
         * <i>{@code REQUIRED}</i> The name of the file to use for the entity data configuration file
         */
        @NotNull public String fileName = "storage.yml";
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingResource.Options options} for the {@link #fileName entity data configuration file}
         * <p>If not specified, the {@link AnnoyingResource.Options default options} will be used
         */
        @Nullable public AnnoyingResource.Options fileOptions = null;

        /**
         * Constructs a new {@link ConfigFile} instance with default values
         */
        public ConfigFile() {
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
        public static DataOptions.ConfigFile load(@NotNull ConfigurationSection section) {
            final ConfigFile options = new ConfigFile();
            if (section.contains("fileName")) options.fileName(section.getString("fileName"));
            if (section.contains("fileOptions")) options.fileOptions(AnnoyingResource.Options.load(section.getConfigurationSection("fileOptions")));
            return options;
        }

        /**
         * Sets {@link #fileName}
         *
         * @param   fileName    the new value
         *
         * @return              this {@link ConfigFile} instance for chaining
         */
        @NotNull
        public DataOptions.ConfigFile fileName(@NotNull String fileName) {
            this.fileName = fileName;
            return this;
        }

        /**
         * Sets {@link #fileOptions}
         *
         * @param   fileOptions the new value
         *
         * @return              this {@link ConfigFile} instance for chaining
         */
        @NotNull
        public DataOptions.ConfigFile fileOptions(@Nullable AnnoyingResource.Options fileOptions) {
            this.fileOptions = fileOptions;
            return this;
        }

        /**
         * Sets {@link #fileOptions} using the specified {@link Consumer}
         *
         * @param   consumer    the {@link Consumer} to accept the new {@link #fileOptions}
         *
         * @return              this {@link ConfigFile} instance for chaining
         */
        @NotNull
        public DataOptions.ConfigFile fileOptions(@NotNull Consumer<AnnoyingResource.Options> consumer) {
            final AnnoyingResource.Options options = new AnnoyingResource.Options();
            consumer.accept(options);
            return fileOptions(options);
        }
    }
}
