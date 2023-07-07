package xyz.srnyx.annoyingapi.options;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.parents.Stringable;


public class BStatsOptions extends Stringable {
    /**
     * <i>{@code RECOMMENDED}</i> The ID of the plugin on <a href="https://bstats.org">bStats</a>
     * <p>If not specified, bStats metrics will not be enabled
     */
    @Nullable public Integer id = null;
    /**
     * <i>{@code REQUIRED}</i> The name of the file to use for the <a href="https://bstats.org">bStats</a> toggle
     * <p>This is required as bStats requires a way to disable metrics. Tampering with this in such a way that removes the ability to disable metrics will result in your plugin being banned from bStats
     * <p><b>IF YOU CHANGE THIS:</b> The file MUST have {@link #toggleKey} for a {@code boolean} to toggle bStats. If it doesn't, bStats will always be disabled
     */
    @NotNull public String fileName = "bstats.yml";
    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingResource.Options options} for the {@link #fileName bStats} file
     * <p>If not specified, the default options will be used
     */
    @Nullable public AnnoyingResource.Options fileOptions = null;
    @NotNull public String toggleKey = "enabled";

    /**
     * Constructs a new {@link BStatsOptions} instance with default values
     */
    public BStatsOptions() {
        // Only exists to give the constructor a Javadoc
    }

    @NotNull
    public static BStatsOptions load(@NotNull ConfigurationSection section) {
        final BStatsOptions options = new BStatsOptions();
        if (section.contains("id")) options.id = section.getInt("bStatsId");
        if (section.contains("fileName")) options.fileName = section.getString("bStatsFileName");
        if (section.contains("fileOptions")) options.fileOptions = AnnoyingResource.Options.load(section.getConfigurationSection("bStatsFileOptions"));
        if (section.contains("toggleKey")) options.toggleKey = section.getString("bStatsToggleKey");
        return options;
    }

    /**
     * Sets {@link #id}
     *
     * @param   id  the new {@link #id}
     *
     * @return      the {@link BStatsOptions} instance for chaining
     */
    @NotNull
    public BStatsOptions id(@Nullable Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Sets {@link #fileName}
     *
     * @param   fileName    the new {@link #fileName}
     *
     * @return              the {@link BStatsOptions} instance for chaining
     */
    @NotNull
    public BStatsOptions fileName(@NotNull String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * Sets {@link #fileOptions}
     *
     * @param   fileOptions the new {@link #fileOptions}
     *
     * @return              the {@link BStatsOptions} instance for chaining
     */
    @NotNull
    public BStatsOptions fileOptions(@Nullable AnnoyingResource.Options fileOptions) {
        this.fileOptions = fileOptions;
        return this;
    }

    /**
     * Sets {@link #toggleKey}
     *
     * @param   toggleKey   the new {@link #toggleKey}
     *
     * @return              the {@link BStatsOptions} instance for chaining
     */
    @NotNull
    public BStatsOptions toggleKey(@NotNull String toggleKey) {
        this.toggleKey = toggleKey;
        return this;
    }
}
