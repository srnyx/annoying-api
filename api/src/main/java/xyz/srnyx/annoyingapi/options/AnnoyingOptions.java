package xyz.srnyx.annoyingapi.options;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.parents.Stringable;

import java.io.Reader;
import java.util.function.Consumer;


/**
 * Represents the options for the API
 */
@SuppressWarnings("CanBeFinal")
public class AnnoyingOptions extends Stringable {
    /**
     * Some general plugin options
     */
    @NotNull public PluginOptions pluginOptions = new PluginOptions();
    /**
     * Options for class registration (commands/listeners/etc...)
     */
    @NotNull public RegistrationOptions registrationOptions = new RegistrationOptions();
    /**
     * Options for <a href="https://bstats.org/">bStats</a>
     */
    @NotNull public BStatsOptions bStatsOptions = new BStatsOptions();
    /**
     * Options for the messages file ({@code messages.yml} by default)
     */
    @NotNull public MessagesOptions messagesOptions = new MessagesOptions();

    /**
     * Constructs a new {@link AnnoyingOptions} instance with default values
     */
    public AnnoyingOptions() {
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
    public static AnnoyingOptions load(@NotNull ConfigurationSection section) {
        final AnnoyingOptions options = new AnnoyingOptions();
        if (section.contains("pluginOptions")) options.pluginOptions = PluginOptions.load(section.getConfigurationSection("pluginOptions"));
        if (section.contains("registrationOptions")) options.registrationOptions = RegistrationOptions.load(section.getConfigurationSection("registrationOptions"));
        if (section.contains("bStatsOptions")) options.bStatsOptions = BStatsOptions.load(section.getConfigurationSection("bStatsOptions"));
        if (section.contains("messagesOptions")) options.messagesOptions = MessagesOptions.load(section.getConfigurationSection("messagesOptions"));
        return options;
    }

    /**
     * Loads the options from the specified {@link Reader}
     *
     * @param   reader  the reader to load the options from
     *
     * @return          the loaded options
     */
    @NotNull
    public static AnnoyingOptions load(@NotNull Reader reader) {
        final ConfigurationSection annoying = YamlConfiguration.loadConfiguration(reader).getConfigurationSection("annoying");
        return annoying != null ? load(annoying) : new AnnoyingOptions();
    }

    /**
     * Sets {@link #pluginOptions}
     *
     * @param   pluginOptions   the new {@link #pluginOptions}
     *
     * @return                  the {@link AnnoyingOptions} instance for chaining
     */
    @NotNull
    public AnnoyingOptions pluginOptions(@NotNull PluginOptions pluginOptions) {
        this.pluginOptions = pluginOptions;
        return this;
    }

    /**
     * Sets {@link #pluginOptions}
     *
     * @param   consumer    the consumer to accept the {@link PluginOptions}
     *
     * @return              the {@link AnnoyingOptions} instance for chaining
     */
    @NotNull
    public AnnoyingOptions pluginOptions(@NotNull Consumer<PluginOptions> consumer) {
        consumer.accept(pluginOptions);
        return this;
    }

    /**
     * Sets {@link #bStatsOptions}
     *
     * @param   bStatsOptions   the new {@link #bStatsOptions}
     *
     * @return                  the {@link AnnoyingOptions} instance for chaining
     */
    @NotNull
    public AnnoyingOptions bStatsOptions(@NotNull BStatsOptions bStatsOptions) {
        this.bStatsOptions = bStatsOptions;
        return this;
    }

    /**
     * Sets {@link #bStatsOptions}
     *
     * @param   consumer    the consumer to accept the {@link BStatsOptions}
     *
     * @return              the {@link AnnoyingOptions} instance for chaining
     */
    @NotNull
    public AnnoyingOptions bStatsOptions(@NotNull Consumer<BStatsOptions> consumer) {
        consumer.accept(bStatsOptions);
        return this;
    }

    /**
     * Sets {@link #messagesOptions}
     *
     * @param   messagesOptions the new {@link #messagesOptions}
     *
     * @return                  the {@link AnnoyingOptions} instance for chaining
     */
    @NotNull
    public AnnoyingOptions messagesOptions(@NotNull MessagesOptions messagesOptions) {
        this.messagesOptions = messagesOptions;
        return this;
    }

    /**
     * Sets {@link #messagesOptions}
     *
     * @param   consumer    the consumer to accept the {@link MessagesOptions}
     *
     * @return              the {@link AnnoyingOptions} instance for chaining
     */
    @NotNull
    public AnnoyingOptions messagesOptions(@NotNull Consumer<MessagesOptions> consumer) {
        consumer.accept(messagesOptions);
        return this;
    }

    /**
     * Sets {@link #registrationOptions}
     *
     * @param   registrationOptions the new {@link #registrationOptions}
     *
     * @return                      the {@link AnnoyingOptions} instance for chaining
     */
    @NotNull
    public AnnoyingOptions registrationOptions(@NotNull RegistrationOptions registrationOptions) {
        this.registrationOptions = registrationOptions;
        return this;
    }

    /**
     * Sets {@link #registrationOptions}
     *
     * @param   consumer    the consumer to accept the {@link RegistrationOptions}
     *
     * @return              the {@link AnnoyingOptions} instance for chaining
     */
    @NotNull
    public AnnoyingOptions registrationOptions(@NotNull Consumer<RegistrationOptions> consumer) {
        consumer.accept(registrationOptions);
        return this;
    }
}
