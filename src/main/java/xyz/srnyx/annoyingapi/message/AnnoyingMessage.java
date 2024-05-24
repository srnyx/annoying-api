package xyz.srnyx.annoyingapi.message;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.options.MessagesOptions;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;

import xyz.srnyx.javautilities.parents.Stringable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static xyz.srnyx.annoyingapi.reflection.net.md_5.bungee.api.chat.RefClickEvent.RefAction.COPY_TO_CLIPBOARD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.entity.RefPlayer.PLAYER_SEND_TITLE_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.entity.RefPlayer.RefSpigot.PLAYER_SPIGOT_SEND_MESSAGE_METHOD;


/**
 * Represents a message from the {@link MessagesOptions#fileName} file
 */
public class AnnoyingMessage extends Stringable {
    /**
     * The {@link AnnoyingPlugin} instance
     */
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * The key of the message in the messages file
     */
    @NotNull private final String key;
    /**
     * Whether to parse PAPI placeholders
     */
    private final boolean parsePapiPlaceholders;
    /**
     * The replacements for the message
     */
    @NotNull private final Set<Replacement> replacements = new HashSet<>();
    /**
     * The cached splitter for placeholder parameters
     */
    @Nullable private String splitterPlaceholder;
    /**
     * Cached components to improve performance
     * <br>Only used if the message doesn't contain {@code %command%}
     * <br>This is reset if {@link #replacements} are modified
     */
    @Nullable private BaseComponent[] components;

    /**
     * Constructs a new {@link AnnoyingMessage} with the specified key
     *
     * @param   plugin                  {@link #plugin}
     * @param   key                     {@link #key}
     * @param   parsePapiPlaceholders   {@link #parsePapiPlaceholders}
     */
    public AnnoyingMessage(@NotNull AnnoyingPlugin plugin, @NotNull String key, boolean parsePapiPlaceholders) {
        this.plugin = plugin;
        this.key = key;
        this.parsePapiPlaceholders = parsePapiPlaceholders;
        plugin.globalPlaceholders.forEach((placeholder, value) -> replace("%" + placeholder + "%", value));
    }

    /**
     * Constructs a new {@link AnnoyingMessage} with the specified key and PAPI placeholders enabled
     *
     * @param   plugin  {@link #plugin}
     * @param   key     {@link #key}
     */
    public AnnoyingMessage(@NotNull AnnoyingPlugin plugin, @NotNull String key) {
        this(plugin, key, true);
    }

    /**
     * Constructs a new {@link AnnoyingMessage} from another {@link AnnoyingMessage} (copy constructor)
     *
     * @param   message the {@link AnnoyingMessage} to copy
     */
    public AnnoyingMessage(@NotNull AnnoyingMessage message) {
        this.plugin = message.plugin;
        this.key = message.key;
        this.parsePapiPlaceholders = message.parsePapiPlaceholders;
        this.replacements.addAll(message.replacements);
        this.splitterPlaceholder = message.splitterPlaceholder;
        this.components = message.components;
    }

    /**
     * Constructs a new {@link AnnoyingMessage} from another {@link AnnoyingMessage} with a new key
     *
     * @param   message the {@link AnnoyingMessage} to copy
     * @param   newKey  the new key to use
     */
    public AnnoyingMessage(@NotNull AnnoyingMessage message, @NotNull String newKey) {
        this.plugin = message.plugin;
        this.key = newKey;
        this.parsePapiPlaceholders = message.parsePapiPlaceholders;
        this.replacements.addAll(message.replacements);
        this.splitterPlaceholder = message.splitterPlaceholder;
    }

    /**
     * {@link #replace(String, Object)} except using parameter placeholders
     *
     * @param   placeholder the placeholder to replace (must have {@code %} on both sides)
     * @param   value       the {@link Object} to replace with
     * @param   type        the {@link DefaultReplaceType} to use. If {@code null}, the replacement will be treated normally
     *
     * @return              the updated {@link AnnoyingMessage} instance
     *
     * @see                 AnnoyingMessage#replace(String, Object)
     * @see                 ReplaceType
     */
    @NotNull
    public AnnoyingMessage replace(@NotNull String placeholder, @Nullable Object value, @Nullable ReplaceType type) {
        if (components != null) components = null; // Remove cached components
        replacements.add(new Replacement(placeholder, value, type));
        return this;
    }

    /**
     * Replace a {@link String} in the message with another {@link Object}
     *
     * @param   before  the {@link String} to replace
     * @param   after   the {@link Object} to replace with
     *
     * @return          the updated {@link AnnoyingMessage} instance
     *
     * @see             #replace(String, Object, xyz.srnyx.annoyingapi.message.ReplaceType)
     */
    @NotNull
    public AnnoyingMessage replace(@NotNull String before, @Nullable Object after) {
        return replace(before, after, null);
    }

    /**
     * Gets the message in {@link BaseComponent}s
     *
     * @param   sender  the {@link AnnoyingSender} to use
     *
     * @return          the message in {@link BaseComponent}s
     *
     * @see             #send(AnnoyingSender)
     */
    @NotNull
    public BaseComponent[] getComponents(@Nullable AnnoyingSender sender) {
        if (components != null) return components; // Use cached components
        final AnnoyingJSON json = new AnnoyingJSON();

        // Get messages file
        final AnnoyingResource messages = plugin.messages;
        if (messages == null) return json.build();
        replaceCommand(sender);

        // Get player, splitterJson, & section
        final Player player = sender == null || !sender.isPlayer ? null : sender.getPlayer();
        final String splitterJson = plugin.getMessagesString(plugin.options.messagesOptions.keys.splitterJson);
        final ConfigurationSection section = messages.getConfigurationSection(key);

        // Single component
        if (section == null) {
            String string = messages.getString(key);
            if (string == null) return json.append(key, "&cCheck &4" + plugin.options.messagesOptions.fileName + "&c!").build();

            // Process replacements
            for (final Replacement replacement : replacements) string = replacement.process(string);
            if (parsePapiPlaceholders) string = plugin.parsePapiPlaceholders(player, string);

            // Build components
            final String[] split = string.split(splitterJson, 3);
            final BaseComponent[] newComponents = json.append(split[0], extractHover(split), ClickEvent.Action.SUGGEST_COMMAND, extractFunction(split)).build();

            // Cache (if %command% not present) & return components
            if (!string.contains("%command%")) components = newComponents;
            return newComponents;
        }

        // Multiple components
        boolean shouldCache = true;
        for (final Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            final String subKey = entry.getKey();
            final Object value = entry.getValue();
            if (!(value instanceof String)) {
                json.append(key + "." + subKey, "&cCheck &4" + plugin.options.messagesOptions.fileName + "&c!");
                continue;
            }
            String subMessage = (String) value;

            // Only cache if %command% not present
            if (shouldCache && subMessage.contains("%command%")) shouldCache = false;

            // Process replacements
            for (final Replacement replacement : replacements) subMessage = replacement.process(subMessage);
            if (parsePapiPlaceholders) subMessage = plugin.parsePapiPlaceholders(player, subMessage);

            // Get component parts
            final String[] split = subMessage.split(splitterJson, 3);
            final String display = split[0];
            final String hover = extractHover(split);
            final String function = extractFunction(split);

            // No function component
            if (function == null) {
                json.append(display, hover);
                continue;
            }

            // Prompt component
            if (subKey.startsWith("suggest")) {
                json.append(display, hover, ClickEvent.Action.SUGGEST_COMMAND, function);
                continue;
            }

            // Clipboard component
            if (COPY_TO_CLIPBOARD != null && subKey.startsWith("copy")) {
                json.append(display, hover, COPY_TO_CLIPBOARD, function);
                continue;
            }

            // Chat component
            if (subKey.startsWith("chat")) {
                json.append(display, hover, ClickEvent.Action.RUN_COMMAND, function);
                continue;
            }

            // Web component
            if (subKey.startsWith("web")) {
                json.append(display, hover, ClickEvent.Action.OPEN_URL, function);
                continue;
            }

            // Text component
            json.append(display, hover);
        }

        // Build, cache, & return components
        final BaseComponent[] newComponents = json.build();
        if (shouldCache) components = newComponents;
        return newComponents;
    }

    /**
     * Runs {@link #getComponents(AnnoyingSender)} using {@code null}
     *
     * @return  the message in {@link BaseComponent}s
     *
     * @see     #send(AnnoyingSender)
     * @see     #getComponents(AnnoyingSender)
     */
    @NotNull
    public BaseComponent[] getComponents() {
        return getComponents(null);
    }

    /**
     * Gets the message using {@link #getComponents(AnnoyingSender)} then joins the {@link BaseComponent}s together
     * <p>This will only have the display text of the components, use {@link #getComponents(AnnoyingSender)} for all parts
     *
     * @param   sender  the {@link AnnoyingSender} to use
     *
     * @return          the message
     *
     * @see             #getComponents(AnnoyingSender)
     */
    @NotNull
    public String toString(@Nullable AnnoyingSender sender) {
        final StringBuilder builder = new StringBuilder();
        for (final BaseComponent component : getComponents(sender)) builder.append(component.toLegacyText());
        return builder.toString();
    }

    /**
     * Runs {@link #toString(AnnoyingSender)} using {@code null}
     * <p>This will only have the display text of the components, use {@link #getComponents()} for all parts
     *
     * @return  the message
     *
     * @see     #getComponents()
     * @see     #toString(AnnoyingSender)
     */
    @Override @NotNull
    public String toString() {
        return toString(null);
    }

    /**
     * Broadcasts the message with the specified {@link BroadcastType} and title parameters. {@code fadeIn}, {@code stay}, and {@code fadeOut} are 1.11+ only and will be ignored on older versions
     *
     * @param   type    the {@link BroadcastType} to broadcast with
     * @param   sender  the {@link AnnoyingSender} to use
     * @param   fadeIn  the fade in time for the title
     * @param   stay    the stay time for the title
     * @param   fadeOut the fade out time for the title
     *
     * @see             #broadcast(BroadcastType, Integer, Integer, Integer)
     * @see             #broadcast(BroadcastType, AnnoyingSender)
     * @see             #broadcast(BroadcastType)
     */
    public void broadcast(@NotNull BroadcastType type, @Nullable AnnoyingSender sender, @Nullable Integer fadeIn, @Nullable Integer stay, @Nullable Integer fadeOut) {
        // Title/subtitle/full title
        if (type.isTitle()) {
            if (fadeIn == null) fadeIn = 10;
            if (stay == null) stay = 70;
            if (fadeOut == null) fadeOut = 20;

            // Title/subtitle
            if (type.equals(BroadcastType.TITLE) || type.equals(BroadcastType.SUBTITLE)) {
                final String message = toString();
                // Title
                if (type.equals(BroadcastType.TITLE)) {
                    broadcastTitle(message, "", fadeIn, stay, fadeOut);
                    return;
                }
                // Subtitle
                broadcastTitle("", message, fadeIn, stay, fadeOut);
                return;
            }

            // Title and subtitle (full title)
            broadcastTitle(
                    new AnnoyingMessage(this, key + ".title").toString(sender),
                    new AnnoyingMessage(this, key + ".subtitle").toString(sender),
                    fadeIn, stay, fadeOut);
            return;
        }
        final BaseComponent[] components = getComponents(sender);

        // Action bar
        if (type.equals(BroadcastType.ACTIONBAR) && PLAYER_SPIGOT_SEND_MESSAGE_METHOD != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                try {
                    PLAYER_SPIGOT_SEND_MESSAGE_METHOD.invoke(player.spigot(), ChatMessageType.ACTION_BAR, components);
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        // Chat
        Bukkit.spigot().broadcast(components);
    }

    /**
     * Broadcasts the message with the specified {@link BroadcastType} and title parameters
     * <p>This is equivalent to calling {@link #broadcast(BroadcastType, AnnoyingSender, Integer, Integer, Integer)} with {@code null} for {@code sender}
     *
     * @param   type    the {@link BroadcastType} to broadcast with
     * @param   fadeIn  the fade in time for the title
     * @param   stay    the stay time for the title
     * @param   fadeOut the fade out time for the title
     *
     * @see             #broadcast(BroadcastType, AnnoyingSender, Integer, Integer, Integer)
     */
    public void broadcast(@NotNull BroadcastType type, @Nullable Integer fadeIn, @Nullable Integer stay, @Nullable Integer fadeOut) {
        broadcast(type, null, fadeIn, stay, fadeOut);
    }

    /**
     * Broadcasts the message with the specified {@link BroadcastType}, {@link AnnoyingSender}, and default title parameters
     * <p>This is equivalent to calling {@link #broadcast(BroadcastType, AnnoyingSender, Integer, Integer, Integer)} with {@code null} for all title parameters
     *
     * @param   type    the {@link BroadcastType} to broadcast with
     * @param   sender  the {@link AnnoyingSender} to use
     *
     * @see             #broadcast(BroadcastType, AnnoyingSender, Integer, Integer, Integer)
     */
    public void broadcast(@NotNull BroadcastType type, @Nullable AnnoyingSender sender) {
        broadcast(type, sender, null, null, null);
    }

    /**
     * Broadcasts the message with the specified {@link BroadcastType} and default title parameters
     * <p>This is equivalent to calling {@link #broadcast(BroadcastType, AnnoyingSender, Integer, Integer, Integer)} with {@code null} for all other parameters
     *
     * @param   type    the {@link BroadcastType} to broadcast with
     *
     * @see             #broadcast(BroadcastType, AnnoyingSender, Integer, Integer, Integer)
     */
    public void broadcast(@NotNull BroadcastType type) {
        broadcast(type, null, null, null, null);
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
        // Player (JSON)
        if (sender.isPlayer) {
            sender.getPlayer().spigot().sendMessage(getComponents(sender));
            return;
        }

        // Console/non-player (normal)
        sender.cmdSender.sendMessage(toString(sender));
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
        send(new AnnoyingSender(plugin, sender));
    }

    /**
     * Logs the message to the console with the specified {@link Level}
     *
     * @param   level   the {@link Level} to log with
     */
    public void log(@Nullable Level level) {
        AnnoyingPlugin.log(level, toString());
    }

    /**
     * Runs {@link #log(Level)} using {@code null}
     */
    public void log() {
        log(null);
    }

    /**
     * Adds the {@link Replacement replacement} for {@code %command%}
     *
     * @param   sender  the {@link AnnoyingSender} to use
     */
    private void replaceCommand(@Nullable AnnoyingSender sender) {
        final StringBuilder command = new StringBuilder();
        if (sender != null) {
            if (sender.label != null) command.append("/").append(sender.label);
            if (sender.args != null && sender.args.length != 0) command.append(" ").append(String.join(" ", sender.args));
        }
        replace("%command%", command.toString());
    }

    /**
     * Extracts the hover component from the specified {@link String} array. This will return {@code null} if the hover component is empty (stripped of color)
     *
     * @param   split   the {@link String} array to extract the hover component from
     *
     * @return          the hover component, or {@code null} if the hover component is empty
     */
    @Nullable
    private String extractHover(@NotNull String[] split) {
        final String hover = split.length >= 2 ? split[1] : null;
        return hover != null && BukkitUtility.stripUntranslatedColor(hover).isEmpty() ? null : hover;
    }

    /**
     * Extracts the function from the specified {@link String} array. This will return {@code null} if the function is empty (stripped of color)
     *
     * @param   split   the {@link String} array to extract the function from
     *
     * @return          the function, or {@code null} if the function is empty
     */
    @Nullable
    private String extractFunction(@NotNull String[] split) {
        final String function = split.length >= 3 ? split[2] : null;
        return function != null && BukkitUtility.stripUntranslatedColor(function).isEmpty() ? null : function;
    }

    /**
     * Broadcasts the specified title and subtitle to all online players. {@code fadeIn}, {@code stay}, and {@code fadeOut} are 1.11+ only and will be ignored on older versions
     *
     * @param   title       the title to broadcast
     * @param   subtitle    the subtitle to broadcast
     * @param   fadeIn      the fade in time for the title
     * @param   stay        the stay time for the title
     * @param   fadeOut     the fade out time for the title
     *
     * @see                 #broadcast(BroadcastType, Integer, Integer, Integer)
     * @see                 #broadcast(BroadcastType)
     */
    private void broadcastTitle(@NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
        final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (PLAYER_SEND_TITLE_METHOD != null) {
            try {
                for (final Player player : players) PLAYER_SEND_TITLE_METHOD.invoke(player, title, subtitle, fadeIn, stay, fadeOut);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return;
        }
        //noinspection deprecation
        players.forEach(player -> player.sendTitle(title, subtitle));
    }

    /**
     * Used in {@link #replace(String, Object, xyz.srnyx.annoyingapi.message.ReplaceType)} and {@link #replace(String, Object)}
     */
    private class Replacement extends Stringable {
        @NotNull private final String before;
        @NotNull private final String value;
        @Nullable private final ReplaceType type;

        /**
         * Constructs a new {@link Replacement}
         *
         * @param   before  the text to replace. If {@code type} isn't {@code null}, this should be a placeholder ({@code %} around it)
         * @param   value   the value to replace the text with
         * @param   type    the {@link ReplaceType} to use on the value, if {@code null}, the {@code value} will be used as-is
         */
        public Replacement(@NotNull String before, @Nullable Object value, @Nullable xyz.srnyx.annoyingapi.message.ReplaceType type) {
            this.before = before;
            this.value = String.valueOf(value);
            this.type = type;
        }

        /**
         * Performs the replacement on the specified {@link String}
         *
         * @param   input   the {@link String} to perform the replacement on
         *
         * @return          the {@link String} with the replacement performed
         */
        @NotNull
        public String process(@NotNull String input) {
            // Normal placeholder
            if (type == null) return input.replace(before, value);

            // Parameter placeholder
            if (splitterPlaceholder == null) splitterPlaceholder = plugin.getMessagesString(plugin.options.messagesOptions.keys.splitterPlaceholder);
            final Matcher matcher = Pattern.compile("%" + Pattern.quote(before.replace("%", "") + splitterPlaceholder) + ".*?%").matcher(input);
            final String match;
            final String parameter;
            if (matcher.find()) { // find the placeholder (%<placeholder><splitter><input>%) in the message
                match = matcher.group(); // get the placeholder
                final String split = match.split(splitterPlaceholder, 2)[1]; // get the input part of the placeholder
                parameter = split.substring(0, split.length() - 1); // remove the closing % from the input part
            } else {
                match = before; // use the original placeholder
                parameter = type.getDefaultInput(); // use the default input
            }
            return input.replace(match, type.getOutputOperator().apply(parameter, value)); // replace the placeholder with the formatted value
        }
    }
}
