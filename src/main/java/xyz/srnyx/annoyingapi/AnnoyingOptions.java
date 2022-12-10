package xyz.srnyx.annoyingapi;

import com.olliez4.interface4.util.json.JSON;

import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import org.jetbrains.annotations.NotNull;

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
     * <i>{@code OPTIONAL}</i> The primary color used by the API (e.g. for start messages)
     */
    @NotNull public ChatColor colorLight = ChatColor.AQUA;

    /**
     * <i>{@code OPTIONAL}</i> The secondary color used by the API (e.g. for start messages)
     */
    @NotNull public ChatColor colorDark = ChatColor.DARK_AQUA;

    /**
     * <i>{@code RECOMMENDED}</i> The file name of the messages file <i>(usually {@code messages.yml})</i>
     * <p>If not specified, no messages will be loaded (plugin will still enable)
     */
    @NotNull public String messagesFileName = "messages.yml";

    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's prefix
     * <p>AnnoyingAPI will turn this into the message from the resource file
     */
    @NotNull public String prefix = "plugin.prefix";

    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's {@link JSON} component splitter
     * <p>AnnoyingAPI will turn this into the message from the resource file
     */
    @NotNull public String splitterJson = "plugin.splitters.json";

    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's placeholder component splitter
     * <p>AnnoyingAPI will turn this into the message from the resource file
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
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingCommand}s to register (add to this {@link Set})
     */
    @NotNull public final Set<AnnoyingCommand> commands = new HashSet<>();

    /**
     * <i>{@code OPTIONAL}</i> The {@link Listener}s to register (add to this {@link Set})
     */
    @NotNull public final Set<AnnoyingListener> listeners = new HashSet<>();

    /**
     * <i>{@code OPTIONAL}</i> Dependencies of the API and the plugin (add to this {@link Set})
     */
    @NotNull public final List<AnnoyingDependency> dependencies = new ArrayList<>();
}
