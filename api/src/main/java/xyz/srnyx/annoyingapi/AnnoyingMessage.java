package xyz.srnyx.annoyingapi;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    @NotNull
    public AnnoyingMessage replace(@NotNull String before, @Nullable Object after) {
        replacements.put(before, String.valueOf(after));
        return this;
    }

    /**
     * {@link #replace(String, Object)} except with custom formatting
     * 
     * @param   before  the {@link String} to replace
     * @param   after   the {@link Object} to replace with
     * @param   type    the {@link DefaultReplaceType} to use
     * 
     * @return          the updated {@link AnnoyingMessage} instance
     *
     * @see             AnnoyingMessage#replace(String, Object)
     * @see             ReplaceType
     */
    @NotNull
    public AnnoyingMessage replace(@NotNull String before, @Nullable Object after, @NotNull ReplaceType type) {
        final String regex = "%" + Pattern.quote(before.replace("%", "") + plugin.options.splitterPlaceholder) + ".*?%";
        final Matcher matcher = Pattern.compile(regex).matcher(toString());
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
        replacements.put(match, type.getOutputOperator().apply(input, String.valueOf(after))); // replace the placeholder with the formatted value
        return this;
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
            final String label = sender.getLabel();
            final String[] args = sender.getArgs();
            if (label != null) command.append("/").append(label);
            if (args != null && args.length != 0) command.append(" ").append(String.join(" ", args));
        }
        replace("%command%", command.toString());

        final ConfigurationSection section = messages.getConfigurationSection(key);
        if (section == null) {
            final String[] split = messages.getString(key, key).split(plugin.options.splitterJson, 3);
            String display = split[0];

            // Message
            if (split.length == 1) {
                for (final Map.Entry<String, String> entry : replacements.entrySet()) display = display.replace(entry.getKey(), entry.getValue());
                json.append(display);
                return json.build();
            }
            String hover = split[1];

            // Message with hover
            if (split.length == 2) {
                for (final Map.Entry<String, String> entry : replacements.entrySet()) {
                    final String before = entry.getKey();
                    final String after = entry.getValue();
                    display = display.replace(before, after);
                    hover = hover.replace(before, after);
                }
                json.append(display, hover);
                return json.build();
            }

            // Message with hover and click
            String click = split[2];
            for (final Map.Entry<String, String> entry : replacements.entrySet()) {
                final String before = entry.getKey();
                final String after = entry.getValue();
                display = display.replace(before, after);
                hover = hover.replace(before, after);
                click = click.replace(before, after);
            }
            json.append(display, hover, ClickEvent.Action.SUGGEST_COMMAND, click);
            return json.build();
        }

        // Multiple components
        for (final String subKey : section.getKeys(false)) {
            String subMessage = section.getString(subKey);
            if (subMessage == null) {
                json.append(key + "." + subKey, "&cCheck &4" + plugin.options.messagesFileName + "&c!");
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
                    for (final Player online : Bukkit.getOnlinePlayers()) online.sendTitle(message, "", fadeIn, stay, fadeOut);
                    return;
                }

                // Subtitle
                for (final Player online : Bukkit.getOnlinePlayers()) online.sendTitle("", message, fadeIn, stay, fadeOut);
                return;
            }

            // Title and subtitle (full title)
            final AnnoyingMessage titleMessage = new AnnoyingMessage(plugin, key + ".title");
            final AnnoyingMessage subtitleMessage = new AnnoyingMessage(plugin, key + ".subtitle");
            for (final Map.Entry<String, String> entry : replacements.entrySet()) {
                titleMessage.replace(entry.getKey(), entry.getValue());
                subtitleMessage.replace(entry.getKey(), entry.getValue());
            }
            final String titleString = titleMessage.toString();
            final String subtitleString = subtitleMessage.toString();
            for (final Player online : Bukkit.getOnlinePlayers()) online.sendTitle(titleString, subtitleString, fadeIn, stay, fadeOut);
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
     * Sends the message to the specified {@link AnnoyingSender}
     *
     * @param   sender  the {@link AnnoyingSender} to send the message to
     *
     * @see             #send(CommandSender)
     * @see             #getComponents(AnnoyingSender)
     */
    public void send(@NotNull AnnoyingSender sender) {
        final CommandSender cmdSender = sender.getCmdSender();

        // Player (JSON)
        if (cmdSender instanceof Player) {
            sender.getPlayer().spigot().sendMessage(getComponents(sender));
            return;
        }

        // Console/non-player (normal)
        cmdSender.sendMessage(toString(sender));
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
        @Contract(pure = true)
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
        @Override @NotNull @Contract(pure = true)
        public String getDefaultInput() {
            return defaultInput;
        }

        /**
         * Gets the {@link BinaryOperator} to use on the input and value
         *
         * @return  the {@link BinaryOperator} to use on the input and value
         */
        @Override @NotNull @Contract(pure = true)
        public BinaryOperator<String> getOutputOperator() {
            return outputOperator;
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
        @Contract(pure = true)
        public boolean isTitle() {
            return this == TITLE || this == SUBTITLE || this == FULL_TITLE;
        }
    }
}
