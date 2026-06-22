package xyz.srnyx.annoyingapi.message;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.message.json.AnnoyingJSON;
import xyz.srnyx.annoyingapi.message.json.message.JsonChatMessage;
import xyz.srnyx.annoyingapi.message.json.message.JsonMessage;
import xyz.srnyx.annoyingapi.message.json.message.JsonTitleMessage;
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
 * Represents a message from the messages file
 */
public class AnnoyingMessage extends Stringable {
    /**
     * The {@link AnnoyingPlugin} instance
     */
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * The value of the message in the messages file
     */
    @NotNull private final JsonMessage jsonMessage;
    /**
     * Whether to parse PAPI placeholders
     */
    private boolean parsePapiPlaceholders;
    /**
     * The replacements for the message
     */
    @NotNull private final Set<Replacement> replacements = new HashSet<>();
    /**
     * Cached components to improve performance
     * <br>Only used if the message doesn't contain {@code %command%}
     * <br>This is reset if {@link #parsePapiPlaceholders} or {@link #replacements} are modified
     */
    @NotNull private BaseComponent @Nullable [] components;

    public AnnoyingMessage(@NotNull JsonMessage jsonMessage) {
        this.plugin = jsonMessage.plugin;
        this.jsonMessage = jsonMessage;
        plugin.getAnnoyingMessages().plugin.global_placeholders.forEach((placeholder, placeholderValue) -> replace("%" + placeholder + "%", placeholderValue));
    }

    public AnnoyingMessage(@NotNull AnnoyingMessage message, @NotNull JsonMessage newMessage) {
        this.plugin = message.plugin;
        this.jsonMessage = newMessage;
        this.parsePapiPlaceholders = message.parsePapiPlaceholders;
        this.replacements.addAll(message.replacements);
    }

    public AnnoyingMessage(@NotNull AnnoyingMessage message, @NotNull String newMessage) {
        this.plugin = message.plugin;
        this.jsonMessage = new JsonChatMessage(plugin, newMessage);
        this.parsePapiPlaceholders = message.parsePapiPlaceholders;
        this.replacements.addAll(message.replacements);
    }

    /**
     * Constructs a new {@link AnnoyingMessage} from another {@link AnnoyingMessage} (copy constructor)
     *
     * @param   message the {@link AnnoyingMessage} to copy
     */
    public AnnoyingMessage(@NotNull AnnoyingMessage message) {
        this(message, message.jsonMessage);
    }

    /**
     * @see #parsePapiPlaceholders
     */
    @NotNull
    public AnnoyingMessage parsePapiPlaceholders(boolean parsePapiPlaceholders) {
        if (parsePapiPlaceholders == this.parsePapiPlaceholders) return this;
        if (components != null) components = null; // Remove cached components
        this.parsePapiPlaceholders = parsePapiPlaceholders;
        return this;
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
        // Use cached components
        if (components != null) return components;

        // Cast to JsonChatMessage
        if (!(jsonMessage instanceof JsonChatMessage chatMessage)) throw new IllegalStateException("Message is not a chat message");

        // Add %command% replacement
        replaceCommand(sender);

        // Get player
        final Player player = sender == null || !sender.isPlayer ? null : sender.getPlayer();

        // Get JSON components
        final AnnoyingJSON json = new AnnoyingJSON();
        for (final Map.Entry<String, String> entry : chatMessage.components.entrySet()) {
            final String key = entry.getKey();
            String value = entry.getValue();

            // Process replacements
            for (final Replacement replacement : replacements) value = replacement.process(value);

            // Parse PAPI placeholders
            if (parsePapiPlaceholders) value = plugin.parsePapiPlaceholders(player, value);

            final String[] split = value.split(plugin.getAnnoyingMessages().plugin.splitters.json, 3);
            // Text
            final String text = split[0];
            // Hover
            final String splitHover = split.length >= 2 ? split[1] : null;
            final String hover = splitHover != null && BukkitUtility.stripUntranslatedColor(splitHover).isEmpty() ? null : splitHover;
            // Action
            final String splitAction = split.length >= 3 ? split[2] : null;
            final String actionValue = splitAction != null && BukkitUtility.stripUntranslatedColor(splitAction).isEmpty() ? null : splitAction;

            if (actionValue != null) {
                // Suggest
                if (key.startsWith("suggest")) {
                    json.append(text, hover, ClickEvent.Action.SUGGEST_COMMAND, actionValue);
                    continue;
                }

                // Copy
                if (COPY_TO_CLIPBOARD != null && key.startsWith("copy")) {
                    json.append(text, hover, COPY_TO_CLIPBOARD, actionValue);
                    continue;
                }

                // Chat
                if (key.startsWith("chat")) {
                    json.append(text, hover, ClickEvent.Action.RUN_COMMAND, actionValue);
                    continue;
                }

                // Web
                if (key.startsWith("web")) {
                    json.append(text, hover, ClickEvent.Action.OPEN_URL, actionValue);
                    continue;
                }
            }

            // Text
            json.append(text, hover);
        }

        // Build, cache, & return components
        final BaseComponent[] newComponents = json.build();
        if (chatMessage.shouldCache()) components = newComponents;
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
            if (!(jsonMessage instanceof JsonTitleMessage titleMessage)) throw new IllegalStateException("DEVELOPER: JsonMessage is not a JsonTitleMessage");
            broadcastTitle(
                    new AnnoyingMessage(this, titleMessage.title).toString(sender),
                    new AnnoyingMessage(this, titleMessage.subtitle).toString(sender),
                    fadeIn, stay, fadeOut);
            return;
        }
        final BaseComponent[] compiledComponents = getComponents(sender);

        // Action bar
        if (type.equals(BroadcastType.ACTIONBAR) && PLAYER_SPIGOT_SEND_MESSAGE_METHOD != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                try {
                    PLAYER_SPIGOT_SEND_MESSAGE_METHOD.invoke(player.spigot(), ChatMessageType.ACTION_BAR, compiledComponents);
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        // Chat
        Bukkit.spigot().broadcast(compiledComponents);
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
        replace("%command%", sender != null ? "/" + sender.getFullCommand() : "");
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
            final String placeholderSplitter = plugin.getAnnoyingMessages().plugin.splitters.placeholder;
            final Matcher matcher = Pattern.compile("%" + Pattern.quote(before.replace("%", "") + placeholderSplitter) + ".*?%").matcher(input);
            final String match;
            final String parameter;
            if (matcher.find()) { // find the placeholder (%<placeholder><splitter><input>%) in the message
                match = matcher.group(); // get the placeholder
                final String split = match.split(placeholderSplitter, 2)[1]; // get the input part of the placeholder
                parameter = split.substring(0, split.length() - 1); // remove the closing % from the input part
            } else {
                match = before; // use the original placeholder
                parameter = type.getDefaultInput(); // use the default input
            }
            return input.replace(match, type.getOutputOperator().apply(parameter, value)); // replace the placeholder with the formatted value
        }
    }
}
