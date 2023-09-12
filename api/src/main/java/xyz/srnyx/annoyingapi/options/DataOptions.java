package xyz.srnyx.annoyingapi.options;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.file.AnnoyingFile;
import xyz.srnyx.annoyingapi.data.EntityData;

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.function.Consumer;


/**
 * These options all relate to data management utilities such as {@link EntityData}
 * <p>You only need to worry about these if your plugin supposed 1.13.2 or lower
 */
public class DataOptions extends Stringable {
    /**
     * Options for {@link EntityData entity data management}
     */
    @NotNull public Entities entities = new Entities();

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
        if (section.contains("entities")) options.entities(Entities.load(section.getConfigurationSection("entities")));
        return options;
    }

    /**
     * Options for {@link EntityData entity data management}
     */
    public static class Entities {
        /**
         * The path to the folder (inside {@code plugins/PLUGIN/data/}) where the entity data files will be stored
         */
        @NotNull public String path = "entities";
        /**
         * The options for all entity data files
         */
        @Nullable public AnnoyingFile.Options<?> fileOptions = new AnnoyingFile.Options<>().canBeEmpty(false);
        /**
         * The YML section/node where the entity data will be stored
         */
        @NotNull public String section = "data";

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
         */
        @NotNull
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
         */
        @NotNull
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
         */
        @NotNull
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
         */
        @NotNull
        public Entities node(@NotNull String node) {
            this.section = node;
            return this;
        }
    }
}
