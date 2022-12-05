package xyz.srnyx.annoyingapi;

import com.olliez4.interface4.util.json.JSON;

import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.util.HashSet;
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
     * <i>{@code RECOMMENDED}</i> The {@link AnnoyingResource} containing the plugin's messages <i>(usually just {@code messages.yml})</i>
     * <p>If not specified, no messages will be loaded (plugin will still enable)
     */
    @Nullable public AnnoyingResource messages;
    /**
     * <i>{@code OPTIONAL}</i> The {@link #messages} key for the plugin's prefix
     */
    @NotNull public String prefix = "plugin.prefix";
    /**
     * <i>{@code OPTIONAL}</i> The {@link #messages} key for the plugin's {@link JSON} component splitter
     */
    @NotNull public String splitterJson = "plugin.splitters.json";
    /**
     * <i>{@code OPTIONAL}</i> The {@link #messages} key for the plugin's placeholder component splitter
     */
    @NotNull public String splitterPlaceholder = "plugin.splitters.placeholder";
    /**
     * <i>{@code OPTIONAL}</i> The {@link #messages} key for the plugin's "no permission" message
     */
    @NotNull public String noPermission = "error.no-permission";
    /**
     * <i>{@code OPTIONAL}</i> The {@link #messages} key for the plugin's "player-only" message
     */
    @NotNull public String onlyPlayer = "error.only-player";
    /**
     * <i>{@code OPTIONAL}</i> The {@link #messages} key for the plugin's "invalid arguments" message
     */
    @NotNull public String invalidArguments = "error.invalid-arguments";
    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingCommand}s to register
     */
    @NotNull public Set<AnnoyingCommand> commands = new HashSet<>();
    /**
     * <i>{@code OPTIONAL}</i> The {@link Listener}s to register
     */
    @NotNull public Set<AnnoyingListener> listeners = new HashSet<>();
}
