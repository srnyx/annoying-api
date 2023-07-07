package xyz.srnyx.annoyingapi.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.file.MessagesFormat;
import xyz.srnyx.annoyingapi.options.MessagesOptions;
import xyz.srnyx.annoyingapi.parents.Stringable;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;
import xyz.srnyx.annoyingapi.utility.adventure.AdventureUtility;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Represents a message from the {@link MessagesOptions#fileName} file
 */
public class AnnoyingMessage extends Stringable {
    @NotNull private static final TextComponent MINIMESSAGE_SECTION_HOVER = Component.text()
                .append(Component.text("You're trying to use ", NamedTextColor.RED))
                .append(Component.text("LEGACY", NamedTextColor.DARK_RED))
                .append(Component.text(" JSON components with a ", NamedTextColor.RED))
                .append(Component.text("MINIMESSAGE", NamedTextColor.DARK_RED))
                .append(Component.text(" format!", NamedTextColor.RED))
                .build();

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
     * Constructs a new {@link AnnoyingMessage} with the specified key
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
    }

    /**
     * {@link #replace(String, Component)} except using parameter placeholders
     * 
     * @param   before  the placeholder to replace (must have {@code %} on both sides)
     * @param   after   the {@link Component} to replace with
     * @param   type    the {@link DefaultReplaceType} to use. If {@code null}, the replacement will be treated normally
     * 
     * @return          the updated {@link AnnoyingMessage} instance
     *
     * @see             AnnoyingMessage#replace(String, Component)
     * @see             ReplaceType
     */
    @NotNull
    public AnnoyingMessage replace(@NotNull String before, @Nullable Component after, @Nullable ReplaceType type) {
        replacements.add(new Replacement(before, after, type));
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
     * @see             #replace(String, Component, ReplaceType)
     */
    @NotNull
    public AnnoyingMessage replace(@NotNull String before, @Nullable Component after) {
        return replace(before, after, null);
    }

    @NotNull
    public AnnoyingMessage replace(@NotNull String before, @Nullable Object after, @Nullable ReplaceType type) {
        replacements.add(new Replacement(before, AdventureUtility.convertMiniMessage(String.valueOf(after)), type));
        return this;
    }

    @NotNull
    public AnnoyingMessage replace(@NotNull String before, @Nullable Object after) {
        return replace(before, after, null);
    }

    @NotNull
    public Component getComponent(@Nullable AnnoyingSender sender) {
        final AnnoyingResource messages = plugin.messages;
        if (messages == null) return Component.empty();
        final Player player = sender == null || !sender.isPlayer ? null : sender.getPlayer();
        final ConfigurationSection section = messages.getConfigurationSection(key);
        final String splitterJson = plugin.messages.getString(plugin.options.messagesOptions.keys.splitterJson, plugin.options.messagesOptions.keys.splitterJson);

        // Replace %command%
        final TextComponent.Builder command = Component.text();
        if (sender != null) {
            if (sender.label != null) command.append(Component.text("/" + sender.label));
            if (sender.args != null && sender.args.length != 0) command.append(Component.text(" " + String.join(" ", sender.args)));
        }
        replace("%command%", command.build());

        if (section == null) {
            // MINIMESSAGE
            if (plugin.messagesFormat.equals(MessagesFormat.MINIMESSAGE)) {
                final Component component = processComponent(player, AdventureUtility.convertMiniMessage(messages.getString(key, key)));
                if (component.equals(Component.text(key))) return nullMessage(key);
                return component;
            }

            // LEGACY: Single component
            final String string = processString(player, messages.getString(key, key));
            if (string.equals(key)) return nullMessage(key);
            final String[] split = string.split(splitterJson, 3);
            // Display
            final TextComponent.Builder display = extractDisplay(split).toBuilder();
            // Hover
            final TextComponent hover = extractHover(split);
            if (hover != null) display.hoverEvent(hover);
            // Function
            final String function = extractFunction(split);
            if (function != null) display.clickEvent(ClickEvent.suggestCommand(function));
            // Return
            return display.build();
        }

        // MINIMESSAGE: Multiple components
        if (plugin.messagesFormat.equals(MessagesFormat.MINIMESSAGE)) return Component.text(key).hoverEvent(MINIMESSAGE_SECTION_HOVER);

        // LEGACY: Multiple components
        final TextComponent.Builder component = Component.text();
        for (final String subKey : section.getKeys(false)) {
            final String subMessage = processString(player, messages.getString(subKey, subKey));
            if (subMessage.equals(subKey)) {
                component.append(nullMessage(key + "." + subKey));
                continue;
            }

            // Get component parts
            final String[] split = subMessage.split(splitterJson, 3);
            final TextComponent display = extractDisplay(split);
            final TextComponent hover = extractHover(split);
            final String function = extractFunction(split);

            // No function component
            if (function == null) {
                component.append(display).hoverEvent(hover);
                continue;
            }

            // Clipboard component
            if (subKey.startsWith("copy")) {
                component.append(display).hoverEvent(hover).clickEvent(ClickEvent.copyToClipboard(function));
                continue;
            }

            // Chat component
            if (subKey.startsWith("chat")) {
                component.append(display).hoverEvent(hover).clickEvent(ClickEvent.runCommand(function));
                continue;
            }

            // Web component
            if (subKey.startsWith("web")) {
                component.append(display).hoverEvent(hover).clickEvent(ClickEvent.openUrl(function));
                continue;
            }

            // Prompt component
            component.append(display).hoverEvent(hover).clickEvent(ClickEvent.suggestCommand(function));
        }
        return component.build();
    }

    @NotNull
    public Component getComponent() {
        return getComponent(null);
    }

    @NotNull
    public TextComponent.Builder getBuilder(@Nullable AnnoyingSender sender) {
        return Component.text().append(getComponent(sender));
    }

    @NotNull
    public TextComponent.Builder getBuilder() {
        return getBuilder(null);
    }

    /**
     * Gets the message using {@link #getComponent(AnnoyingSender)} and converts it to a MiniMessage {@link String}
     *
     * @param   sender  the {@link AnnoyingSender} to use
     *
     * @return          the message
     *
     * @see             #getComponent(AnnoyingSender)
     */
    @NotNull
    public String toString(@Nullable AnnoyingSender sender) {
        return AdventureUtility.convertMiniMessage(getComponent(sender));
    }

    /**
     * Runs {@link #toString(AnnoyingSender)} using {@code null}
     * <p>This will only have the display text of the components, use {@link #getComponent()} for all parts
     *
     * @return  the message
     *
     * @see     #getComponent()
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
     * @param   fadeIn  the fade in time for the title
     * @param   stay    the stay time for the title
     * @param   fadeOut the fade out time for the title
     *
     * @see             #broadcast(BroadcastType)
     */
    public void broadcast(@NotNull BroadcastType type, @Nullable Duration fadeIn, @Nullable Duration stay, @Nullable Duration fadeOut) {
        final Audience all = plugin.audiences.all();

        // Title/subtitle/full title
        if (type.isTitle()) {
            if (fadeIn == null) fadeIn = Duration.ofMillis(500);
            if (stay == null) stay = Duration.ofMillis(3500);
            if (fadeOut == null) fadeOut = Duration.ofSeconds(1);

            // Title
            if (type.equals(BroadcastType.TITLE)) {
                all.showTitle(Title.title(
                        getComponent(),
                        Component.empty(),
                        Title.Times.times(fadeIn, stay, fadeOut)));
                return;
            }

            // Subtitle
            if (type.equals(BroadcastType.SUBTITLE)) {
                all.showTitle(Title.title(
                        Component.empty(),
                        getComponent(),
                        Title.Times.times(fadeIn, stay, fadeOut)));
                return;
            }

            // Title and subtitle (full title)
            final AnnoyingMessage titleMessage = new AnnoyingMessage(plugin, key + ".title");
            final AnnoyingMessage subtitleMessage = new AnnoyingMessage(plugin, key + ".subtitle");
            titleMessage.replacements.addAll(replacements);
            subtitleMessage.replacements.addAll(replacements);
            all.showTitle(Title.title(
                    titleMessage.getComponent(),
                    subtitleMessage.getComponent(),
                    Title.Times.times(fadeIn, stay, fadeOut)));
            return;
        }

        // Action bar
        if (type.equals(BroadcastType.ACTIONBAR)) {
            all.sendActionBar(getComponent());
            return;
        }

        // Chat
        all.sendMessage(getComponent());
    }

    public void broadcast(@NotNull BroadcastType type, @Nullable Title.Times times) {
        final boolean timesProvided = times != null;
        broadcast(type, timesProvided ? times.fadeIn() : null, timesProvided ? times.stay() : null, timesProvided ? times.fadeOut() : null);
    }

    /**
     * Broadcasts the message with the specified {@link BroadcastType} and default title parameters
     * <p>This is equivalent to calling {@link #broadcast(BroadcastType, Duration, Duration, Duration)} with {@code null} for all title parameters
     *
     * @param   type    the {@link BroadcastType} to broadcast with
     *
     * @see             #broadcast(BroadcastType, Duration, Duration, Duration)
     */
    public void broadcast(@NotNull BroadcastType type) {
        broadcast(type, null);
    }

    /**
     * Sends the message to the specified {@link AnnoyingSender}
     *
     * @param   sender  the {@link AnnoyingSender} to send the message to
     *
     * @see             #send(CommandSender)
     * @see             #getComponent(AnnoyingSender)
     */
    public void send(@NotNull AnnoyingSender sender) {
        plugin.audiences.sender(sender.cmdSender).sendMessage(getComponent(sender));
    }

    /**
     * Sends the message to the specified {@link CommandSender}
     * <p>This will convert the {@link CommandSender} to a {@link AnnoyingSender} and then run {@link #send(AnnoyingSender)}
     *
     * @param   sender  the {@link CommandSender} to send the message to
     *
     * @see             #send(AnnoyingSender)
     */
    public void send(@NotNull CommandSender sender){
        send(new AnnoyingSender(plugin, sender));
    }

    public void log(@Nullable Level level) {
        AnnoyingPlugin.log(level, getComponent());
    }

    public void log() {
        log(null);
    }

    @NotNull
    private TextComponent nullMessage(@NotNull String keyUsed) {
        return Component.text(keyUsed).hoverEvent(Component.text("Check ", NamedTextColor.RED))
                .append(Component.text(plugin.options.messagesOptions.fileName, NamedTextColor.DARK_RED))
                .append(Component.text("!", NamedTextColor.RED));
    }

    @NotNull
    private Component processComponent(@Nullable Player player, @NotNull Component component) {
        for (final Replacement replacement : replacements) component = replacement.process(component);
        //TODO if (parsePapiPlaceholders) component = plugin.parsePapiPlaceholders(player, component);
        return component;
    }

    @NotNull
    private String processString(@Nullable Player player, @NotNull String string) {
        for (final Replacement replacement : replacements) string = AdventureUtility.convertLegacy(replacement.process(AdventureUtility.convertLegacy(string)));
        if (parsePapiPlaceholders) string = plugin.parsePapiPlaceholders(player, string);
        return string;
    }

    @NotNull
    private TextComponent extractDisplay(@NotNull String[] split) {
        return AdventureUtility.convertLegacy(split[0]);
    }

    /**
     * Extracts the hover component from the specified {@link String} array. This will return {@code null} if the hover component is empty (stripped of color)
     *
     * @param   split   the {@link String} array to extract the hover component from
     *
     * @return          the hover component, or {@code null} if the hover component is empty
     */
    @Nullable
    private TextComponent extractHover(@NotNull String[] split) {
        final String hover = split.length >= 2 ? split[1] : null;
        return hover == null || BukkitUtility.stripUntranslatedColor(hover).isEmpty() ? null : AdventureUtility.convertLegacy(hover);
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
     * Used in {@link #replace(String, Component, ReplaceType)} and {@link #replace(String, Component)}
     */
    private class Replacement extends Stringable {
        @NotNull private final String before;
        @Nullable private final Component after;
        @Nullable private final ReplaceType type;

        /**
         * Constructs a new {@link Replacement}
         *
         * @param   before  the text to replace. If {@code type} isn't {@code null}, this should be a placeholder ({@code %} around it)
         * @param   after   the value to replace the text with
         * @param   type    the {@link ReplaceType} to use on the value, if {@code null}, the {@code value} will be used as-is
         */
        public Replacement(@NotNull String before, @Nullable Component after, @Nullable ReplaceType type) {
            this.before = before;
            this.after = after;
            this.type = type;
        }

        @NotNull
        public Component process(@NotNull Component input) {
            // Normal placeholder
            if (after == null || type == null || plugin.messages == null) return replace(input, before, after);

            // Parameter placeholder
            if (splitterPlaceholder == null) splitterPlaceholder = plugin.messages.getString(plugin.options.messagesOptions.keys.splitterPlaceholder, plugin.options.messagesOptions.keys.splitterPlaceholder);
            final Matcher matcher = Pattern.compile("%" + Pattern.quote(before.replace("%", "") + splitterPlaceholder) + ".*?%").matcher(AdventureUtility.convertMiniMessage(input));
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
            return replace(input, match, AdventureUtility.convertMiniMessage(type.getOutputOperator().apply(parameter, AdventureUtility.convertMiniMessage(after)))); // replace the placeholder with the formatted value
        }

        @NotNull
        private Component replace(@NotNull Component input, @NotNull String before, @Nullable Component after) {
            return input.replaceText(builder -> builder
                    .matchLiteral(before)
                    .replacement(after));
        }
    }
}
