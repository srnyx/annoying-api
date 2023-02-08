package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.command.AnnoyingCommand;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Represents the options for the API
 */
public class AnnoyingOptions {
    /**
     * <i>{@code RECOMMENDED}</i> The file name of the messages file <i>(usually {@code messages.yml})</i>
     * <p>If not specified, no messages will be loaded (plugin will still enable)
     */
    @NotNull public String messagesFileName = "messages.yml";

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
     * Constructs a new {@link AnnoyingOptions} instance
     */
    public AnnoyingOptions() {
        // Only exists to give the constructor a Javadoc
    }
}
