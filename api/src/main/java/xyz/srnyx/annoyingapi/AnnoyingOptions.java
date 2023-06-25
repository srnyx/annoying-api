package xyz.srnyx.annoyingapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingCommand;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.utility.ConfigurationUtility;

import java.io.Reader;
import java.util.*;
import java.util.function.Supplier;


/**
 * Represents the options for the API
 */
@SuppressWarnings("CanBeFinal")
public class AnnoyingOptions implements Dumpable<ConfigurationSection> {
    /**
     * <i>{@code RECOMMENDED}</i> The ID of the plugin on <a href="https://bstats.org">bStats</a>
     * <p>If not specified, bStats metrics will not be enabled
     */
    @Nullable public Integer bStatsId = null;

    /**
     * <i>{@code REQUIRED}</i> The name of the file to use for the <a href="https://bstats.org">bStats</a> toggle
     * <p>This is required as bStats requires a way to disable metrics. Tampering with this in such a way that removes the ability to disable metrics will result in your plugin being banned from bStats
     * <p><b>IF YOU CHANGE THIS:</b> The file MUST have an {@code enabled} key for a {@code boolean} to toggle bStats. If it doesn't, bStats will always be disabled
     */
    @NotNull public String bStatsFileName = "bstats.yml";

    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingResource.ResourceOptions options} for the {@link #bStatsFileName bStats} file
     * <p>If not specified, the default options will be used
     */
    @Nullable public AnnoyingResource.ResourceOptions bStatsOptions = null;

    /**
     * <i>{@code OPTIONAL}</i>  The file name of the messages file <i>(usually {@code messages.yml})</i>
     * <p>If not specified, no messages will be loaded (plugin will still enable)
     */
    @NotNull public String messagesFileName = "messages.yml";

    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingResource.ResourceOptions options} for the {@link #messagesFileName messages} file
     * <p>If not specified, the default options will be used
     */
    @Nullable public AnnoyingResource.ResourceOptions messagesOptions = null;

    /**
     * <i>{@code OPTIONAL}</i> The different message keys for some default messages in the {@link #messagesFileName messages file}
     */
    @NotNull public MessageKeys messageKeys = new MessageKeys();

    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingCommand}s to register (add commands to this in the plugin's constructor)
     * <p>If you add a command to this OUTSIDE the constructor, it will not be registered
     */
    @NotNull public final Set<AnnoyingCommand> commandsToRegister = new HashSet<>();

    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingListener}s to register (add listeners to this in the plugin's constructor)
     * <p>If you add a listener to this OUTSIDE the constructor, it will not be registered
     */
    @NotNull public final Set<AnnoyingListener> listenersToRegister = new HashSet<>();

    /**
     * <i>{@code OPTIONAL}</i> The {@link PlaceholderExpansion PAPI expansion} to register when the plugin {@link AnnoyingPlugin#onEnable() enables}
     * <p><i>Can also be a {@link AnnoyingPAPIExpansion}</i>
     */
    @Nullable public Supplier<Object> papiExpansionToRegister;

    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingDependency}s to check for (add dependencies to this in the plugin's constructor)
     * <p>If you add a dependency to this OUTSIDE the constructor, it will not be checked
     * <p><i>This is <b>NOT</b> meant for optional dependencies, all of these dependencies will be downloaded/installed (even if {@link AnnoyingDependency#required} is {@code false})</i>
     */
    @NotNull public final List<AnnoyingDependency> dependencies = new ArrayList<>();

    /**
     * <i>{@code RECOMMENDED}</i> The different {@link PluginPlatform platforms} the plugin is available on
     * <p>If not specified, the plugin will not be able to check for updates
     */
    @NotNull public PluginPlatform.Multi updatePlatforms = new PluginPlatform.Multi();

    /**
     * Constructs a new {@link AnnoyingOptions} instance with default values
     */
    public AnnoyingOptions() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * Loads the options from the specified {@link YamlConfiguration}
     *
     * @param   yaml    the configuration to load the options from
     */
    @NotNull
    public static AnnoyingOptions load(@NotNull YamlConfiguration yaml) {
        final ConfigurationSection annoying = yaml.getConfigurationSection("annoying");
        if (annoying == null) throw new IllegalArgumentException("No 'annoying' section found in the configuration");
        return load(annoying);
    }

    /**
     * Loads the options from the specified {@link Reader}
     *
     * @param   reader  the reader to load the options from
     */
    @NotNull
    public static AnnoyingOptions load(@NotNull Reader reader) {
        return load(YamlConfiguration.loadConfiguration(reader));
    }

    @NotNull
    public static AnnoyingOptions load(@NotNull ConfigurationSection section) {
        final AnnoyingOptions options = new AnnoyingOptions();
        if (section.contains("bStatsId")) options.bStatsId = section.getInt("bStatsId");
        if (section.contains("bStatsFileName")) options.bStatsFileName = section.getString("bStatsFileName");
        if (section.contains("bStatsOptions")) options.bStatsOptions = AnnoyingResource.ResourceOptions.load(section.getConfigurationSection("bStatsOptions"));
        if (section.contains("messagesFileName")) options.messagesFileName = section.getString("messagesFileName");
        if (section.contains("messagesOptions")) options.messagesOptions = AnnoyingResource.ResourceOptions.load(section.getConfigurationSection("messagesOptions"));
        options.messageKeys = MessageKeys.load(section.getConfigurationSection("messageKeys"));
        ConfigurationUtility.toConfigurationList(section.getMapList("dependencies")).stream()
                .map(AnnoyingDependency::load)
                .forEach(options.dependencies::add);
        Bukkit.getLogger().severe(ConfigurationUtility.toMapList(options.dependencies.get(0).platforms.dump(new ArrayList<>())).toString());
        options.updatePlatforms = PluginPlatform.Multi.load(ConfigurationUtility.toConfigurationList(section.getMapList("updatePlatforms")));
        return options;
    }

    @Override @NotNull
    public ConfigurationSection dump(@NotNull ConfigurationSection section) {
        section.set("bStatsId", bStatsId);
        section.set("bStatsFileName", bStatsFileName);
        if (bStatsOptions != null) bStatsOptions.dump(section.createSection("bStatsOptions"));
        section.set("messagesFileName", messagesFileName);
        if (messagesOptions != null) messagesOptions.dump(section.createSection("messagesOptions"));
        messageKeys.dump(section.createSection("messageKeys"));
        final ConfigurationSection dependenciesSection = section.createSection("dependencies");
        this.dependencies.forEach(dependency -> dependency.dump(dependenciesSection));
        section.set("updatePlatforms", ConfigurationUtility.toMapList(updatePlatforms.dump(new ArrayList<>())));
        return section;
    }

    /**
     * Casts the {@link #papiExpansionToRegister} to a {@link PlaceholderExpansion} and returns it
     *
     * @return  the {@link #papiExpansionToRegister} as a {@link PlaceholderExpansion} or {@code null} if it is not a {@link PlaceholderExpansion}
     */
    @Nullable
    public PlaceholderExpansion getPapiExpansionToRegister() {
        return papiExpansionToRegister instanceof PlaceholderExpansion ? (PlaceholderExpansion) papiExpansionToRegister.get() : null;
    }

    public static class MessageKeys implements Dumpable<ConfigurationSection> {
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's global placeholders
         *
         * @see AnnoyingPlugin#globalPlaceholders
         */
        @NotNull public String globalPlaceholders = "plugin.global-placeholders";

        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's JSON component splitter
         */
        @NotNull public String splitterJson = "plugin.splitters.json";

        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's placeholder component splitter
         */
        @NotNull public String splitterPlaceholder = "plugin.splitters.placeholder";

        /**
         * <i>{@code OPTIONAL}</i> The key for the message sent in the console when an update is available for the plugin
         */
        @NotNull public String updateAvailable = "plugin.update-available";

        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "no permission" message
         */
        @NotNull public String noPermission = "error.no-permission";

        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "player-only" message
         */
        @NotNull public String playerOnly = "error.player-only";

        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "invalid argument" message
         * <p>This should contain {@code %argument%} for the invalid argument
         */
        @NotNull public String invalidArgument = "error.invalid-argument";

        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "invalid arguments" message
         */
        @NotNull public String invalidArguments = "error.invalid-arguments";

        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "disabled command" message
         */
        @NotNull public String disabledCommand = "error.disabled-command";

        /**
         * Creates a new {@link MessageKeys} with the default values
         */
        public MessageKeys() {
            // Only exists to give the constructor a Javadoc
        }

        @NotNull
        public static MessageKeys load(@NotNull ConfigurationSection section) {
            final MessageKeys keys = new MessageKeys();
            if (section.contains("globalPlaceholders")) keys.globalPlaceholders = section.getString("globalPlaceholders");
            if (section.contains("splitterJson")) keys.splitterJson = section.getString("splitterJson");
            if (section.contains("splitterPlaceholder")) keys.splitterPlaceholder = section.getString("splitterPlaceholder");
            if (section.contains("updateAvailable")) keys.updateAvailable = section.getString("updateAvailable");
            if (section.contains("noPermission")) keys.noPermission = section.getString("noPermission");
            if (section.contains("playerOnly")) keys.playerOnly = section.getString("playerOnly");
            if (section.contains("invalidArgument")) keys.invalidArgument = section.getString("invalidArgument");
            if (section.contains("invalidArguments")) keys.invalidArguments = section.getString("invalidArguments");
            if (section.contains("disabledCommand")) keys.disabledCommand = section.getString("disabledCommand");
            return keys;
        }

        @Override @NotNull
        public ConfigurationSection dump(@NotNull ConfigurationSection section) {
            section.set("globalPlaceholders", globalPlaceholders);
            section.set("splitterJson", splitterJson);
            section.set("splitterPlaceholder", splitterPlaceholder);
            section.set("updateAvailable", updateAvailable);
            section.set("noPermission", noPermission);
            section.set("playerOnly", playerOnly);
            section.set("invalidArgument", invalidArgument);
            section.set("invalidArguments", invalidArguments);
            section.set("disabledCommand", disabledCommand);
            return section;
        }
    }
}
