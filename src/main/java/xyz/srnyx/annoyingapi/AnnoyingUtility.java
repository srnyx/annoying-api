package xyz.srnyx.annoyingapi;

import com.olliez4.interface4.util.json.components.*;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import org.apache.commons.lang.time.DurationFormatUtils;

import org.bukkit.ChatColor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.text.DecimalFormat;


/**
 * General utility methods for AnnoyingAPI
 */
public class AnnoyingUtility {
    /**
     * Gets a string from {@link AnnoyingOptions#messagesFileName} with the specified key
     * <p>If the string is not found, it will return the key
     *
     * @param   plugin  the plugin to get the string from
     * @param   key     the key of the string
     *
     * @return          the string
     */
    @NotNull
    public static String getString(@NotNull AnnoyingPlugin plugin, @NotNull String key) {
        final AnnoyingResource messages = plugin.messages;
        if (messages == null) return key;
        return messages.getString(key, key);
    }

    /**
     * Translates {@code &} color codes to {@link ChatColor}
     *
     * @param   message the message to translate
     *
     * @return          the translated message
     */
    @NotNull @Contract("_ -> new")
    public static String color(@Nullable String message) {
        if (message == null) return "null";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Formats a millisecond long using the given pattern
     *
     * @param   value   the milliseconds to format
     * @param   pattern the pattern to use
     *
     * @return          the formatted time
     */
    @NotNull
    public static String formatMillis(long value, @Nullable String pattern) {
        if (pattern == null) pattern = "m':'s";
        return DurationFormatUtils.formatDuration(value, pattern);
    }

    /**
     * Formats a {@link Double} value using the given pattern
     *
     * @param   value   the {@link Double} to format
     * @param   pattern the pattern to use
     *
     * @return          the formatted value
     */
    @NotNull
    public static String formatDouble(double value, @Nullable String pattern) {
        if (pattern == null) pattern = "#,###.##";
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * Converts a {@link JSONComponent} to a {@link TextComponent}
     *
     * @param   component   the {@link JSONComponent} to convert
     *
     * @return              the {@link TextComponent}
     *
     * @see                 AnnoyingMessage#getBaseComponents(AnnoyingSender)
     */
    @NotNull
    public static TextComponent jsonToTextComponent(@NotNull JSONComponent component) {
        // Display
        final TextComponent textComponent = new TextComponent(AnnoyingUtility.color(component.getDisplay()));
        // Hover
        final String hover = component.getHover();
        if (hover != null) textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(AnnoyingUtility.color(hover))));
        // Function
        final String function = component.getFunctionString();
        if (function != null) {
            // Web
            if (component instanceof JWebComponent) {
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, function));
                return textComponent;
            }
            // Prompt
            if (component instanceof JPromptComponent) {
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, function));
                return textComponent;
            }
            // Command
            if (component instanceof JCommandComponent) {
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + function));
                return textComponent;
            }
            // Clipboard
            if (component instanceof JClipboardComponent) {
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, function));
                return textComponent;
            }
            // Chat
            if (component instanceof JChatComponent) {
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, function));
                return textComponent;
            }
        }

        return textComponent;
    }
}
