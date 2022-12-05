package xyz.srnyx.annoyingapi.message;

import com.olliez4.interface4.util.ActionBar;
import com.olliez4.interface4.util.json.JSON;
import com.olliez4.interface4.util.json.components.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.AnnoyingOptions;
import xyz.srnyx.annoyingapi.AnnoyingSender;
import xyz.srnyx.annoyingapi.AnnoyingUtility;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents a message from the {@link AnnoyingOptions#messages} file.
 */
public class AnnoyingMessage {
    @NotNull private final AnnoyingPlugin plugin;
    @NotNull private final String key;
    @NotNull private String message;
    @NotNull private final String prefix;
    @NotNull private final Map<String, String> replacements = new HashMap<>();

    /**
     * Constructs a new {@link AnnoyingMessage} with the specified key
     *
     * @param   plugin  the plugin getting the message
     * @param   key     the key of the message
     */
    public AnnoyingMessage(@NotNull AnnoyingPlugin plugin, @NotNull String key) {
        this.plugin = plugin;
        this.key = key;
        this.message = getMessage(key);
        this.prefix = getMessage(plugin.options.prefix);
        replace("%prefix%", prefix);
    }

    /**
     * Replace a {@link String} in the message with another {@link Object}
     *
     * @param   before  the {@link String} to replace
     * @param   after   the {@link Object} to replace with
     *
     * @return          the updated {@link AnnoyingMessage} instance
     */
    public AnnoyingMessage replace(@NotNull String before, @Nullable Object after) {
        final String afterString = String.valueOf(after);
        message = message.replace(before, afterString);
        replacements.put(before, afterString);
        return this;
    }

    /**
     * Broadcasts the message with the default {@link AnnoyingBroadcast} and {@link AnnoyingTitle}
     * <p>This is equivalent to calling {@link #broadcast(AnnoyingBroadcast)} with {@code null}
     */
    public void broadcast() {
        broadcast(null);
    }

    /**
     * Broadcasts the message with the specified {@link AnnoyingBroadcast} and default {@link AnnoyingTitle}
     * <p>This is equivalent to calling {@link #broadcast(AnnoyingBroadcast, AnnoyingTitle)} with {@code null} for the {@link AnnoyingTitle}
     *
     * @param   type    the {@link AnnoyingBroadcast} to broadcast with
     */
    public void broadcast(@Nullable AnnoyingBroadcast type) {
        broadcast(type, null);
    }

    /**
     * Broadcasts the message with the specified {@link AnnoyingBroadcast} and {@link AnnoyingTitle}
     *
     * @param   type    the {@link AnnoyingBroadcast} to broadcast with
     * @param   title   the {@link AnnoyingTitle} to broadcast with
     */
    public void broadcast(@Nullable AnnoyingBroadcast type, @Nullable AnnoyingTitle title) {
        if (type == null) type = AnnoyingBroadcast.CHAT;
        if (title == null) title = new AnnoyingTitle();
        final AnnoyingTitle finalTitle = title;

        switch (type) {
            case TITLE:
                Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(message, null, finalTitle.getFadeIn(), finalTitle.getStay(), finalTitle.getFadeOut()));
                break;
            case SUBTITLE:
                Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(null, message, finalTitle.getFadeIn(), finalTitle.getStay(), finalTitle.getFadeOut()));
                break;
            case ACTIONBAR:
                Bukkit.getOnlinePlayers().forEach(player -> ActionBar.send(player, message));
                break;
            default:
                broadcast();
                break;
        }
    }

    /**
     * Sends the message to the specified {@link CommandSender}
     * <p>This will convert the {@link CommandSender} to a {@link AnnoyingSender} and then run {@link #send(AnnoyingSender)}
     *
     * @param   sender  the {@link CommandSender} to send the message to
     */
    public void send(@NotNull CommandSender sender) {
        send(new AnnoyingSender(sender));
    }

    /**
     * Sends the message to the specified {@link AnnoyingSender}
     *
     * @param   annoyingSender  the {@link AnnoyingSender} to send the message to
     */
    public void send(@NotNull AnnoyingSender annoyingSender) {
        final CommandSender sender = annoyingSender.getCmdSender();
        final AnnoyingResource messages = plugin.options.messages;
        if (messages == null) {
            JSON.send(sender, new JTextComponent(message, ChatColor.RED + "No messages file found!"));
            return;
        }
        final String splitter = getMessage(plugin.options.splitter);

        // Replace %command%
        final StringBuilder command = new StringBuilder();
        final String label = annoyingSender.getLabel();
        if (label != null) command.append("/").append(label);
        final String[] args = annoyingSender.getArgs();
        if (args != null) command.append(String.join(" ", args));
        replace("%command%", command.toString());

        // Single component
        final ConfigurationSection section = messages.getConfigurationSection(key);
        if (section == null) {
            message = AnnoyingUtility.color(message);

            // Normal message
            if (!message.contains(splitter)) {
                sender.sendMessage(message);
                return;
            }

            final String[] split = message.split(splitter);
            final String display = split[0];
            final String hover = split[1];

            // Prompt component
            if (split.length == 3) {
                JSON.send(sender, new JPromptComponent(display, hover, split[2]));
                return;
            }

            // Text component
            JSON.send(sender, new JTextComponent(display, hover));
            return;
        }

        // Multiple components
        final ArrayList<JSONComponent> components = new ArrayList<>();
        for (final String subKey : section.getKeys(false)) {
            String subMessage = section.getString(subKey);
            if (subMessage == null) {
                components.add(new JTextComponent(key + "." + subKey, AnnoyingUtility.color("&cCheck &4" + messages.getName() + ".yml&c!")));
                continue;
            }

            // Replacements
            for (final Map.Entry<String, String> entry : replacements.entrySet()) subMessage = subMessage.replace(entry.getKey(), entry.getValue());
            subMessage = AnnoyingUtility.color(subMessage);

            // Get component parts
            final String[] split = subMessage
                    .replace("%prefix%", prefix)
                    .replace("%command%", command)
                    .split(splitter);
            final String display = split[0];
            String hover = null;
            String function = null;
            if (split.length == 2 || split.length == 3) {
                hover = split[1];
                if (split.length == 3) function = split[2];
            }

            // Prompt component
            if (subKey.startsWith("prompt")) {
                components.add(new JPromptComponent(display, hover, function));
                continue;
            }

            // Clipboard component
            if (subKey.startsWith("clipboard")) {
                components.add(new JClipboardComponent(display, hover, function));
                continue;
            }

            // Chat component
            if (subKey.startsWith("chat")) {
                components.add(new JChatComponent(display, hover, function));
                continue;
            }

            // Command component
            if (subKey.startsWith("command")) {
                components.add(new JCommandComponent(display, hover, function));
                continue;
            }

            // Web component
            if (subKey.startsWith("web")) {
                components.add(new JWebComponent(display, hover, function));
                continue;
            }

            // Text component
            components.add(new JTextComponent(display, hover));
        }

        // Send message
        JSON.send(sender, components);
    }

    private String getMessage(@NotNull String msgKey) {
        final AnnoyingResource messages = plugin.options.messages;
        if (messages == null) return msgKey;
        return messages.getString(msgKey, msgKey);
    }
}
