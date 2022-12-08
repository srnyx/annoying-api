package xyz.srnyx.annoyingapi;

import com.olliez4.interface4.util.ActionBar;
import com.olliez4.interface4.util.json.JSON;
import com.olliez4.interface4.util.json.components.*;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.apache.commons.lang.time.DurationFormatUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Represents a message from the {@link AnnoyingOptions#messagesFileName} file
 */
public class AnnoyingMessage {
    @NotNull private final AnnoyingPlugin plugin;
    @NotNull private final String key;
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
        replace("%prefix%", plugin.options.prefix);
    }

    /**
     * Replace a {@link String} in the message with another {@link Object}
     *
     * @param   before  the {@link String} to replace
     * @param   after   the {@link Object} to replace with
     *
     * @return          the updated {@link AnnoyingMessage} instance
     * 
     * @see             #replace(String, Object, ReplaceType)
     */
    public AnnoyingMessage replace(@NotNull String before, @Nullable Object after) {
        replacements.put(before, String.valueOf(after));
        return this;
    }

    /**
     * {@link #replace(String, Object)} except with custom formatting
     * 
     * @param   before  the {@link String} to replace
     * @param   after   the {@link Object} to replace with
     * @param   type    the {@link ReplaceType} to use
     * 
     * @return          the updated {@link AnnoyingMessage} instance
     *
     * @see             AnnoyingMessage#replace(String, Object)
     * @see             ReplaceType
     */
    public AnnoyingMessage replace(@NotNull String before, @Nullable Object after, @NotNull ReplaceType type) {
        final String regex = "%" + Pattern.quote(before.replace("%", "") + plugin.options.splitterPlaceholder) + ".*?%";
        final Matcher matcher = Pattern.compile(regex).matcher(getMessage());
        final String match;
        final String input;
        if (matcher.find()) { // find the placeholder (%<placeholder><splitter><input>%) in the message
            match = matcher.group(); // get the placeholder
            final String split = match.split(plugin.options.splitterPlaceholder)[1]; // get the input part of the placeholder
            input = split.substring(0, split.length() - 1); // remove the closing % from the input part
        } else {
            match = before; // use the original placeholder
            input = type.getDefaultInput(); // use the default input
        }
        replacements.put(match, replaceParameter(after, input, type)); // replace the placeholder with the formatted value
        return this;
    }

    @NotNull
    private String replaceParameter(@Nullable Object after, @NotNull String input, @NotNull ReplaceType type) {
        final String value = String.valueOf(after);

        // Time parameter
        if (type == ReplaceType.TIME) {
            final long afterLong;
            try {
                afterLong = Long.parseLong(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return value;
            }
            return AnnoyingUtility.formatMillis(afterLong, input);
        }

        // Decimal parameter
        if (type == ReplaceType.DECIMAL) {
            final double afterDouble;
            try {
                afterDouble = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return value;
            }
            return AnnoyingUtility.formatDouble(afterDouble, input);
        }

        return value;
    }

    /**
     * Runs {@link #getComponents(AnnoyingSender)} using {@code null}
     *
     * @return  the message in {@link JSONComponent}s
     *
     * @see     #send(AnnoyingSender)
     * @see     #getComponents(AnnoyingSender)
     */
    @NotNull
    public List<JSONComponent> getComponents() {
        return getComponents(null);
    }

    /**
     * Gets the message in {@link JSONComponent}s
     *
     * @param   annoyingSender  the {@link AnnoyingSender} to use
     *
     * @return                  the message in {@link JSONComponent}s
     *
     * @see                     #send(AnnoyingSender)
     */
    @NotNull
    public List<JSONComponent> getComponents(@Nullable AnnoyingSender annoyingSender) {
        final List<JSONComponent> components = new ArrayList<>();

        // Get messages file
        final AnnoyingResource messages = plugin.messages;
        if (messages == null) return components;

        // Replace %command%
        final StringBuilder command = new StringBuilder();
        if (annoyingSender != null) {
            final String label = annoyingSender.getLabel();
            final String[] args = annoyingSender.getArgs();
            if (label != null) command.append("/").append(label);
            if (args != null) command.append(String.join(" ", args));
        }
        replace("%command%", command.toString());

        final ConfigurationSection section = messages.getConfigurationSection(key);
        if (section == null) {
            final String[] split = AnnoyingUtility.getString(plugin, key).split(plugin.options.splitterJson);
            String display = split[0];

            // Replacements
            for (Map.Entry<String, String> entry : replacements.entrySet()) display = display.replace(entry.getKey(), entry.getValue());
            display = AnnoyingUtility.color(display);

            // Add component
            switch (split.length) {
                case 3:
                    components.add(new JPromptComponent(display, AnnoyingUtility.color(split[1]), AnnoyingUtility.color(split[2])));
                    break;
                case 2:
                    components.add(new JTextComponent(display, AnnoyingUtility.color(split[1])));
                    break;
                default:
                    components.add(new JTextComponent(display, null));
                    break;
            }
            return components;
        }

        // Multiple components
        for (final String subKey : section.getKeys(false)) {
            String subMessage = section.getString(subKey);
            if (subMessage == null) {
                components.add(new JTextComponent(key + "." + subKey, AnnoyingUtility.color("&cCheck &4" + plugin.options.messagesFileName + "&c!")));
                continue;
            }

            // Replacements
            for (final Map.Entry<String, String> entry : replacements.entrySet()) subMessage = subMessage.replace(entry.getKey(), entry.getValue());
            subMessage = AnnoyingUtility.color(subMessage);

            // Get component parts
            final String[] split = subMessage
                    .replace("%prefix%", plugin.options.prefix)
                    .replace("%command%", command)
                    .split(plugin.options.splitterJson);
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
        return components;
    }

    /**
     * Runs {@link #getComponents(AnnoyingSender)} using {@code null}
     *
     * @return  the message in {@link BaseComponent}s
     *
     * @see     #getComponents()
     * @see     #getBaseComponents(AnnoyingSender)
     */
    public BaseComponent[] getBaseComponents() {
        return getBaseComponents(null);
    }

    /**
     * Gets the message in {@link BaseComponent}s
     *
     * @param   annoyingSender  the {@link AnnoyingSender} to use
     *
     * @return                  the message in {@link BaseComponent}s
     *
     * @see                     #getComponents(AnnoyingSender)
     */
    public BaseComponent[] getBaseComponents(@Nullable AnnoyingSender annoyingSender) {
        final ComponentBuilder builder = new ComponentBuilder();
        getComponents(annoyingSender).forEach(component -> builder.append(AnnoyingUtility.jsonToTextComponent(component), ComponentBuilder.FormatRetention.NONE));
        return builder.create();
    }

    /**
     * Runs {@link #getMessage(AnnoyingSender)} using {@code null}
     * <p>This will only have the display text of the components, use {@link #getComponents()} or {@link #getBaseComponents()} for all parts
     *
     * @return  the message
     *
     * @see     #getComponents()
     * @see     #getMessage(AnnoyingSender)
     */
    @NotNull
    public String getMessage() {
        return getMessage(null);
    }

    /**
     * Gets the message using {@link #getComponents(AnnoyingSender)} then joins the {@link JSONComponent}s together
     * <p>This will only have the display text of the components, use {@link #getComponents(AnnoyingSender)} or {@link #getBaseComponents(AnnoyingSender)} for all parts
     *
     * @param   sender  the {@link AnnoyingSender} to use
     *
     * @return          the message
     *
     * @see             #getComponents(AnnoyingSender)
     */
    @NotNull
    public String getMessage(@Nullable AnnoyingSender sender) {
        return getComponents(sender).stream()
                .map(JSONComponent::getDisplay)
                .collect(Collectors.joining());
    }

    /**
     * Broadcasts the message with the default {@link BroadcastType} and title parameters
     * <p>This is equivalent to calling {@link #broadcast(BroadcastType)} with {@code null}
     *
     * @see     #broadcast(BroadcastType)
     * @see     #broadcast(BroadcastType, Integer, Integer, Integer)
     */
    public void broadcast() {
        broadcast(null);
    }

    /**
     * Broadcasts the message with the specified {@link BroadcastType} and default title parameters
     * <p>This is equivalent to calling {@link #broadcast(BroadcastType, Integer, Integer, Integer)} with {@code null} for all title parameters
     *
     * @param   type    the {@link BroadcastType} to broadcast with
     *
     * @see             #broadcast()
     * @see             #broadcast(BroadcastType, Integer, Integer, Integer)
     */
    public void broadcast(@Nullable BroadcastType type) {
        broadcast(type, null, null, null);
    }

    /**
     * Broadcasts the message with the specified {@link BroadcastType} and title parameters
     *
     * @param   type    the {@link BroadcastType} to broadcast with
     * @param   fadeIn  the fade in time for the title
     * @param   stay    the stay time for the title
     * @param   fadeOut the fade out time for the title
     *
     * @see             #broadcast()
     * @see             #broadcast(BroadcastType)
     */
    public void broadcast(@Nullable BroadcastType type, @Nullable Integer fadeIn, @Nullable Integer stay, @Nullable Integer fadeOut) {
        if (type == null) type = BroadcastType.CHAT;
        if (fadeIn == null) fadeIn = 20;
        if (stay == null) stay = 20;
        if (fadeOut == null) fadeOut = 20;
        final String message = getMessage();
        final int finalFadeIn = fadeIn;
        final int finalStay = stay;
        final int finalFadeOut = fadeOut;

        switch (type) {
            case TITLE:
                Bukkit.getOnlinePlayers().forEach(player ->
                        player.sendTitle(message, null, finalFadeIn, finalStay, finalFadeOut));
                break;
            case SUBTITLE:
                Bukkit.getOnlinePlayers().forEach(player ->
                        player.sendTitle(null, message, finalFadeIn, finalStay, finalFadeOut));
                break;
            case ACTIONBAR:
                Bukkit.getOnlinePlayers().forEach(player -> ActionBar.send(player, message));
                break;
            default:
                Bukkit.spigot().broadcast(getBaseComponents());
                break;
        }
    }

    /**
     * Sends the message to the specified {@link CommandSender}
     * <p>This will convert the {@link CommandSender} to a {@link AnnoyingSender} and then run {@link #send(AnnoyingSender)}
     *
     * @param   sender  the {@link CommandSender} to send the message to
     *
     * @see             #send(AnnoyingSender)
     */
    public void send(@NotNull CommandSender sender) {
        send(new AnnoyingSender(sender));
    }

    /**
     * Sends the message to the specified {@link AnnoyingSender}
     *
     * @param   sender  the {@link AnnoyingSender} to send the message to
     *
     * @see             #send(CommandSender)
     * @see             #getComponents(AnnoyingSender)
     */
    public void send(@NotNull AnnoyingSender sender) {
        JSON.send(sender.getCmdSender(), new ArrayList<>(getComponents(sender)));
    }

    /**
     * Different replace types for {@link #replace(String, Object, ReplaceType)}
     */
    public enum ReplaceType {
        /**
         * Input is used as the format for {@link DurationFormatUtils#formatDuration(long, String)}
         */
        TIME("hh:ss"),
        DECIMAL("#,###.##");

        @NotNull private final String defaultInput;

        /**
         * Constructs a new {@link ReplaceType}
         *
         * @param   defaultInput    the default input value
         */
        @Contract(pure = true)
        ReplaceType(@NotNull String defaultInput) {
            this.defaultInput = defaultInput;
        }

        /**
         * Gets the default input of the type
         * <p>This is used if the user does not provide a custom input
         *
         * @return  the default input
         */
        @NotNull @Contract(pure = true)
        public String getDefaultInput() {
            return defaultInput;
        }
    }

    /**
     * The different types of broadcasts for an {@link AnnoyingMessage}
     */
    public enum BroadcastType {
        /**
         * Message will be sent in chat
         */
        CHAT,
        /**
         * Message will be sent as a title
         */
        TITLE,
        /**
         * Message will be sent as a subtitle
         */
        SUBTITLE,
        /**
         * Message will be displayed in the action bar
         */
        ACTIONBAR
    }
}
