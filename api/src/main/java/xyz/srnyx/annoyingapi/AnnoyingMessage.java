package xyz.srnyx.annoyingapi;

import me.clip.placeholderapi.PlaceholderAPI;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Represents a message from the {@link AnnoyingOptions#messagesFileName} file
 */
public class AnnoyingMessage {
    @NotNull private final AnnoyingPlugin plugin;
    @NotNull private final String key;
    @Nullable private String splitterPlaceholder;
    @NotNull private final Set<Replacement> replacements = new HashSet<>();

    /**
     * Constructs a new {@link AnnoyingMessage} with the specified key
     *
     * @param   plugin  the plugin getting the message
     * @param   key     the key of the message
     */
    public AnnoyingMessage(@NotNull AnnoyingPlugin plugin, @NotNull String key) {
        this.plugin = plugin;
        this.key = key;
        plugin.globalPlaceholders.forEach((placeholder, value) -> replace("%" + placeholder + "%", value));
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
     * @see             #replace(String, Object, ReplaceType)
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
        final AnnoyingJSON json = new AnnoyingJSON();

        // Get messages file
        final AnnoyingResource messages = plugin.messages;
        if (messages == null) return json.build();

        // Replace %command%
        final StringBuilder command = new StringBuilder();
        if (sender != null) {
            if (sender.label != null) command.append("/").append(sender.label);
            if (sender.args != null && sender.args.length != 0) command.append(" ").append(String.join(" ", sender.args));
        }
        replace("%command%", command.toString());

        // Single component
        final Player player = sender == null ? null : sender.getPlayer();
        final String splitterJson = plugin.getMessagesString(plugin.options.splitterJson);
        final ConfigurationSection section = messages.getConfigurationSection(key);
        if (section == null) {
            String string = messages.getString(key);
            if (string == null) return json.append(key, "&cCheck &4" + plugin.options.messagesFileName + "&c!").build();
            for (final Replacement replacement : replacements) string = replacement.process(string);
            if (plugin.papiInstalled) string = PlaceholderAPI.setPlaceholders(player, string);
            final String[] split = string.split(splitterJson, 3);

            // Message
            final String display = split[0];
            if (split.length == 1) return json.append(display).build();

            // Message with hover
            final String hover = split[1];
            if (split.length == 2) return json.append(display, hover).build();

            // Message with hover and click
            return json.append(display, hover, ClickEvent.Action.SUGGEST_COMMAND, split[2]).build();
        }

        // Multiple components
        for (final String subKey : section.getKeys(false)) {
            String subMessage = section.getString(subKey);
            if (subMessage == null) {
                json.append(key + "." + subKey, "&cCheck &4" + plugin.options.messagesFileName + "&c!");
                continue;
            }
            for (final Replacement replacement : replacements) subMessage = replacement.process(subMessage);
            if (plugin.papiInstalled) subMessage = PlaceholderAPI.setPlaceholders(player, subMessage);

            // Get component parts
            final String[] split = AnnoyingUtility.color(subMessage).split(splitterJson, 3);
            final String display = split[0];
            final String hover = split.length == 2 ? split[1] : null;
            final String function = split.length == 3 ? split[2] : null;

            // Prompt component
            if (subKey.startsWith("suggest")) {
                json.append(display, hover, ClickEvent.Action.SUGGEST_COMMAND, function);
                continue;
            }

            // Clipboard component
            if (subKey.startsWith("copy")) {
                json.append(display, hover, ClickEvent.Action.COPY_TO_CLIPBOARD, function);
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
        return json.build();
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
     * Broadcasts the message with the specified {@link BroadcastType} and title parameters
     *
     * @param   type    the {@link BroadcastType} to broadcast with
     * @param   fadeIn  the fade in time for the title
     * @param   stay    the stay time for the title
     * @param   fadeOut the fade out time for the title
     *
     * @see             #broadcast(BroadcastType)
     */
    public void broadcast(@NotNull BroadcastType type, @Nullable Integer fadeIn, @Nullable Integer stay, @Nullable Integer fadeOut) {
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
            final AnnoyingMessage titleMessage = new AnnoyingMessage(plugin, key + ".title");
            final AnnoyingMessage subtitleMessage = new AnnoyingMessage(plugin, key + ".subtitle");
            titleMessage.replacements.addAll(replacements);
            subtitleMessage.replacements.addAll(replacements);
            broadcastTitle(titleMessage.toString(), subtitleMessage.toString(), fadeIn, stay, fadeOut);
            return;
        }
        final BaseComponent[] components = getComponents();

        // Action bar
        if (type.equals(BroadcastType.ACTIONBAR)) {
            Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components));
            return;
        }

        // Chat
        Bukkit.spigot().broadcast(components);
    }

    /**
     * Broadcasts the message with the specified {@link BroadcastType} and default title parameters
     * <p>This is equivalent to calling {@link #broadcast(BroadcastType, Integer, Integer, Integer)} with {@code null} for all title parameters
     *
     * @param   type    the {@link BroadcastType} to broadcast with
     *
     * @see             #broadcast(BroadcastType, Integer, Integer, Integer)
     */
    public void broadcast(@NotNull BroadcastType type) {
        broadcast(type, null, null, null);
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
        final CommandSender cmdSender = sender.cmdSender;

        // Player (JSON)
        if (cmdSender instanceof Player) {
            sender.getPlayer().spigot().sendMessage(getComponents(sender));
            return;
        }

        // Console/non-player (normal)
        cmdSender.sendMessage(toString(sender));
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

    public static void broadcastTitle(@NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(title, subtitle, fadeIn, stay, fadeOut));
    }

    /**
     * Used in {@link #replace(String, Object, ReplaceType)}
     * <p>Implement this into your own enum to create your own {@link ReplaceType}s for {@link #replace(String, Object, ReplaceType)}
     *
     * @see DefaultReplaceType
     * @see #replace(String, Object, ReplaceType)
     */
    public interface ReplaceType {
        /**
         * If no input is provided, this will be used
         *
         * @return  the default input
         */
        @NotNull
        String getDefaultInput();

        /**
         * The action done with the input and value
         * <p>The input comes first, then the value (for the operands)
         *
         * @return  the {@link BinaryOperator} to use on the input and value
         */
        @NotNull
        BinaryOperator<String> getOutputOperator();
    }

    /**
     * Default replace types for {@link #replace(String, Object, ReplaceType)}
     */
    public enum DefaultReplaceType implements ReplaceType {
        /**
         * Input is used as the format for {@link AnnoyingUtility#formatMillis(long, String, boolean)}
         */
        TIME("hh:ss", (input, value) -> AnnoyingUtility.formatMillis(Long.parseLong(value), input, false)),
        /**
         * Input is used as the format for {@link AnnoyingUtility#formatNumber(Number, String)}
         */
        NUMBER("#,###.##", (input, value) -> AnnoyingUtility.formatNumber(Double.parseDouble(value), input)),
        /**
         * Input is used to turn 'true' or 'false' into the specified value
         */
        BOOLEAN("true//false", (input, value) -> {
            String[] split = input.split("//", 2);
            if (split.length != 2) split = new String[]{"true", "false"};
            return Boolean.parseBoolean(value) ? split[0] : split[1];
        });

        /**
         * The default input for this {@link ReplaceType}
         */
        @NotNull private final String defaultInput;
        /**
         * The {@link BinaryOperator<String>} for this {@link ReplaceType}
         */
        @NotNull private final BinaryOperator<String> outputOperator;

        /**
         * Constructs a new {@link DefaultReplaceType}
         *
         * @param   defaultInput    the default input value
         * @param   outputOperator  the {@link BinaryOperator<String>} to use on the input and value
         */
        DefaultReplaceType(@NotNull String defaultInput, @NotNull BinaryOperator<String> outputOperator) {
            this.defaultInput = defaultInput;
            this.outputOperator = outputOperator;
        }

        /**
         * Gets the default input of the type
         * <p>This is used if the user does not provide a custom input
         *
         * @return  the default input
         */
        @Override @NotNull
        public String getDefaultInput() {
            return defaultInput;
        }

        /**
         * Gets the {@link BinaryOperator} to use on the input and value
         *
         * @return  the {@link BinaryOperator} to use on the input and value
         */
        @Override @NotNull
        public BinaryOperator<String> getOutputOperator() {
            return outputOperator;
        }
    }

    /**
     * Used in {@link #replace(String, Object, ReplaceType)} and {@link #replace(String, Object)}
     */
    private class Replacement {
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
        public Replacement(@NotNull String before, @Nullable Object value, @Nullable ReplaceType type) {
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
            if (splitterPlaceholder == null) splitterPlaceholder = plugin.getMessagesString(plugin.options.splitterPlaceholder);
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

    /**
     * The different types of broadcasts for an {@link AnnoyingMessage}
     */
    public enum BroadcastType {
        /**
         * Message will be sent in chat
         */
        CHAT,
        /**
         * Message will be displayed in the action bar
         */
        ACTIONBAR,
        /**
         * Message will be sent as a title
         */
        TITLE,
        /**
         * Message will be sent as a subtitle
         */
        SUBTITLE,
        /**
         * Only use this if the key has 2 children, "title" and "subtitle"
         * <p>The "title" child will be sent as the title and the "subtitle" child will be sent as the subtitle
         */
        FULL_TITLE;

        /**
         * Whether the broadcast type is a title ({@link #TITLE}, {@link #SUBTITLE}, or {@link #FULL_TITLE}), aka anything that has a {@code fadeIn}, {@code stay}, and {@code fadeOut}
         *
         * @return  true if the broadcast type is a title
         */
        public boolean isTitle() {
            return this == TITLE || this == SUBTITLE || this == FULL_TITLE;
        }
    }
}
