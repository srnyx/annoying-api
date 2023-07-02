package xyz.srnyx.annoyingapi.utility.adventure;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;
import net.kyori.adventure.text.serializer.legacy.Reset;

import org.bukkit.ChatColor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;


/**
 * An enum to provide the same functionality as {@link ChatColor} for MiniMessage
 */
public enum MiniColor {
    /**
     * {@link ChatColor#BLACK} ({@code 0})
     */
    BLACK(NamedTextColor.BLACK, ChatColor.BLACK),
    /**
     * {@link ChatColor#DARK_BLUE} ({@code 1})
     */
    DARK_BLUE(NamedTextColor.DARK_BLUE, ChatColor.DARK_BLUE),
    /**
     * {@link ChatColor#DARK_GREEN} ({@code 2})
     */
    DARK_GREEN(NamedTextColor.DARK_GREEN, ChatColor.DARK_GREEN),
    /**
     * {@link ChatColor#DARK_AQUA} ({@code 3})
     */
    DARK_AQUA(NamedTextColor.DARK_AQUA, ChatColor.DARK_AQUA),
    /**
     * {@link ChatColor#DARK_RED} ({@code 4})
     */
    DARK_RED(NamedTextColor.DARK_RED, ChatColor.DARK_RED),
    /**
     * {@link ChatColor#DARK_PURPLE} ({@code 5})
     */
    DARK_PURPLE(NamedTextColor.DARK_PURPLE, ChatColor.DARK_PURPLE),
    /**
     * {@link ChatColor#GOLD} ({@code 6})
     */
    GOLD(NamedTextColor.GOLD, ChatColor.GOLD),
    /**
     * {@link ChatColor#GRAY} ({@code 7})
     */
    GRAY(NamedTextColor.GRAY, ChatColor.GRAY),
    /**
     * {@link ChatColor#DARK_GRAY} ({@code 8})
     */
    DARK_GRAY(NamedTextColor.DARK_GRAY, ChatColor.DARK_GRAY),
    /**
     * {@link ChatColor#BLUE} ({@code 9})
     */
    BLUE(NamedTextColor.BLUE, ChatColor.BLUE),
    /**
     * {@link ChatColor#GREEN} ({@code a})
     */
    GREEN(NamedTextColor.GREEN, ChatColor.GREEN),
    /**
     * {@link ChatColor#AQUA} ({@code b})
     */
    AQUA(NamedTextColor.AQUA, ChatColor.AQUA),
    /**
     * {@link ChatColor#RED} ({@code c})
     */
    RED(NamedTextColor.RED, ChatColor.RED),
    /**
     * {@link ChatColor#LIGHT_PURPLE} ({@code d})
     */
    LIGHT_PURPLE(NamedTextColor.LIGHT_PURPLE, ChatColor.LIGHT_PURPLE),
    /**
     * {@link ChatColor#YELLOW} ({@code e})
     */
    YELLOW(NamedTextColor.YELLOW, ChatColor.YELLOW),
    /**
     * {@link ChatColor#WHITE} ({@code f})
     */
    WHITE(NamedTextColor.WHITE, ChatColor.WHITE),
    /**
     * {@link ChatColor#MAGIC} ({@code k})
     */
    OBFUSCATED(TextDecoration.OBFUSCATED, ChatColor.MAGIC),
    /**
     * {@link ChatColor#BOLD} ({@code l})
     */
    BOLD(TextDecoration.BOLD, ChatColor.BOLD),
    /**
     * {@link ChatColor#STRIKETHROUGH} ({@code m})
     */
    STRIKETHROUGH(TextDecoration.STRIKETHROUGH, ChatColor.STRIKETHROUGH),
    /**
     * {@link ChatColor#UNDERLINE} ({@code n})
     */
    UNDERLINED(TextDecoration.UNDERLINED, ChatColor.UNDERLINE),
    /**
     * {@link ChatColor#ITALIC} ({@code o})
     */
    ITALIC(TextDecoration.ITALIC, ChatColor.ITALIC),
    /**
     * {@link ChatColor#RESET} ({@code r})
     */
    RESET(Reset.INSTANCE, ChatColor.RESET);

    /**
     * The {@link TextFormat} equivalent of the MiniMessage color
     */
    @NotNull public final TextFormat textFormat;

    /**
     * The {@link ChatColor} equivalent of the MiniMessage color
     */
    @NotNull public final ChatColor chatColor;

    MiniColor(@NotNull TextFormat textFormat, @NotNull ChatColor chatColor) {
        this.textFormat = textFormat;
        this.chatColor = chatColor;
    }

    /**
     * Returns the MiniMessage format of the color
     *
     * @return  the MiniMessage format of the color
     */
    @Override @NotNull
    public String toString() {
        return "<" + textFormat + ">";
    }

    /**
     * Get a {@link MiniColor} from a {@link TextFormat}
     *
     * @param   textFormat  the {@link TextFormat} to get the {@link MiniColor} from
     *
     * @return              the {@link MiniColor} from the {@link TextFormat}
     */
    @Nullable
    public static MiniColor fromTextFormat(@NotNull TextFormat textFormat) {
        return Arrays.stream(values())
                .filter(miniColor -> miniColor.textFormat == textFormat)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get a {@link MiniColor} from a {@link ChatColor}
     *
     * @param   chatColor   the {@link ChatColor} to get the {@link MiniColor} from
     *
     * @return              the {@link MiniColor} from the {@link ChatColor}
     */
    @Nullable
    public static MiniColor fromChatColor(@NotNull ChatColor chatColor) {
        return Arrays.stream(values())
                .filter(miniColor -> miniColor.chatColor == chatColor)
                .findFirst()
                .orElse(null);
    }
}
