package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingCommand;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.util.*;


/**
 * Represents the options for the API
 */
public class AnnoyingOptions {
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
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "invalid arguments" message
     */
    @NotNull public String invalidArguments = "error.invalid-arguments";

    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "disabled command" message
     */
    @NotNull public String disabledCommand = "error.disabled-command";

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
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingDependency}s to check for (add dependencies to this in the plugin's constructor)
     * <p>If you add a dependency to this OUTSIDE the constructor, it will not be checked
     */
    @NotNull public final List<AnnoyingDependency> dependencies = new ArrayList<>();

    /**
     * <i>{@code RECOMMENDED}</i> The different {@link xyz.srnyx.annoyingapi.AnnoyingUpdate.Platform platforms} the plugin is available on
     * <p>If not specified, the plugin will not be able to check for updates
     */
    @NotNull public Map<AnnoyingUpdate.Platform, String> updatePlatforms = new EnumMap<>(AnnoyingUpdate.Platform.class);

    /**
     * Constructs a new {@link AnnoyingOptions} instance
     */
    public AnnoyingOptions() {
        // Only exists to give the constructor a Javadoc
    }
}
