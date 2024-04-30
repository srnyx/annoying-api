package xyz.srnyx.annoyingapi.options;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import xyz.srnyx.javautilities.parents.Stringable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Consumer;


/**
 * Represents the options for the API
 */
@SuppressWarnings("CanBeFinal")
public class AnnoyingOptions extends Stringable {
    /**
     * {@link PluginOptions}
     */
    @NotNull public PluginOptions pluginOptions = new PluginOptions();
    /**
     * {@link RegistrationOptions}
     */
    @NotNull public RegistrationOptions registrationOptions = new RegistrationOptions();
    /**
     * {@link BStatsOptions}
     */
    @NotNull public BStatsOptions bStatsOptions = new BStatsOptions();
    /**
     * {@link DataOptions}
     */
    @NotNull public DataOptions dataOptions = new DataOptions();
    /**
     * {@link MessagesOptions}
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
        final ConfigurationSection pluginOptionsSection = section.getConfigurationSection("pluginOptions");
        if (pluginOptionsSection != null) options.pluginOptions(PluginOptions.load(pluginOptionsSection));
        final ConfigurationSection registrationOptionsSection = section.getConfigurationSection("registrationOptions");
        if (registrationOptionsSection != null) options.registrationOptions(RegistrationOptions.load(registrationOptionsSection));
        final ConfigurationSection bStatsOptionsSection = section.getConfigurationSection("bStatsOptions");
        if (bStatsOptionsSection != null) options.bStatsOptions(BStatsOptions.load(bStatsOptionsSection));
        final ConfigurationSection dataOptionsSection = section.getConfigurationSection("dataOptions");
        if (dataOptionsSection != null) options.dataOptions(DataOptions.load(dataOptionsSection));
        final ConfigurationSection messagesOptionsSection = section.getConfigurationSection("messagesOptions");
        if (messagesOptionsSection != null) options.messagesOptions(MessagesOptions.load(messagesOptionsSection));
        return options;
    }

    /**
     * Loads the options from the specified {@link InputStream}
     *
     * @param   inputStream the input stream to load the options from
     *
     * @return              the loaded options
     */
    @NotNull
    public static AnnoyingOptions load(@Nullable InputStream inputStream) {
        if (inputStream == null) return new AnnoyingOptions();
        final ConfigurationSection annoying = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream)).getConfigurationSection("annoying");
        if (annoying == null) return new AnnoyingOptions();
        return load(annoying);
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
     * Sets {@link #dataOptions}
     *
     * @param   dataOptions the new {@link #dataOptions}
     *
     * @return              the {@link AnnoyingOptions} instance for chaining
     */
    @NotNull
    public AnnoyingOptions dataOptions(@NotNull DataOptions dataOptions) {
        this.dataOptions = dataOptions;
        return this;
    }

    /**
     * Sets {@link #dataOptions}
     *
     * @param   consumer    the consumer to accept the {@link DataOptions}
     *
     * @return              the {@link AnnoyingOptions} instance for chaining
     */
    @NotNull
    public AnnoyingOptions dataOptions(@NotNull Consumer<DataOptions> consumer) {
        consumer.accept(dataOptions);
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
