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
    BLACK("<black>", NamedTextColor.BLACK, ChatColor.BLACK),
    /**
     * {@link ChatColor#DARK_BLUE} ({@code 1})
     */
    DARK_BLUE("<dark_blue>", NamedTextColor.DARK_BLUE, ChatColor.DARK_BLUE),
    /**
     * {@link ChatColor#DARK_GREEN} ({@code 2})
     */
    DARK_GREEN("<dark_green>", NamedTextColor.DARK_GREEN, ChatColor.DARK_GREEN),
    /**
     * {@link ChatColor#DARK_AQUA} ({@code 3})
     */
    DARK_AQUA("<dark_aqua>", NamedTextColor.DARK_AQUA, ChatColor.DARK_AQUA),
    /**
     * {@link ChatColor#DARK_RED} ({@code 4})
     */
    DARK_RED("<dark_red>", NamedTextColor.DARK_RED, ChatColor.DARK_RED),
    /**
     * {@link ChatColor#DARK_PURPLE} ({@code 5})
     */
    DARK_PURPLE("<dark_purple>", NamedTextColor.DARK_PURPLE, ChatColor.DARK_PURPLE),
    /**
     * {@link ChatColor#GOLD} ({@code 6})
     */
    GOLD("<gold>", NamedTextColor.GOLD, ChatColor.GOLD),
    /**
     * {@link ChatColor#GRAY} ({@code 7})
     */
    GRAY("<gray>", NamedTextColor.GRAY, ChatColor.GRAY),
    /**
     * {@link ChatColor#DARK_GRAY} ({@code 8})
     */
    DARK_GRAY("<dark_gray>", NamedTextColor.DARK_GRAY, ChatColor.DARK_GRAY),
    /**
     * {@link ChatColor#BLUE} ({@code 9})
     */
    BLUE("<blue>", NamedTextColor.BLUE, ChatColor.BLUE),
    /**
     * {@link ChatColor#GREEN} ({@code a})
     */
    GREEN("<green>", NamedTextColor.GREEN, ChatColor.GREEN),
    /**
     * {@link ChatColor#AQUA} ({@code b})
     */
    AQUA("<aqua>", NamedTextColor.AQUA, ChatColor.AQUA),
    /**
     * {@link ChatColor#RED} ({@code c})
     */
    RED("<red>", NamedTextColor.RED, ChatColor.RED),
    /**
     * {@link ChatColor#LIGHT_PURPLE} ({@code d})
     */
    LIGHT_PURPLE("<light_purple>", NamedTextColor.LIGHT_PURPLE, ChatColor.LIGHT_PURPLE),
    /**
     * {@link ChatColor#YELLOW} ({@code e})
     */
    YELLOW("<yellow>", NamedTextColor.YELLOW, ChatColor.YELLOW),
    /**
     * {@link ChatColor#WHITE} ({@code f})
     */
    WHITE("<white>", NamedTextColor.WHITE, ChatColor.WHITE),
    /**
     * {@link ChatColor#MAGIC} ({@code k})
     */
    OBFUSCATED("<obfuscated>", TextDecoration.OBFUSCATED, ChatColor.MAGIC),
    /**
     * {@link ChatColor#BOLD} ({@code l})
     */
    BOLD("<bold>", TextDecoration.BOLD, ChatColor.BOLD),
    /**
     * {@link ChatColor#STRIKETHROUGH} ({@code m})
     */
    STRIKETHROUGH("<strikethrough>", TextDecoration.STRIKETHROUGH, ChatColor.STRIKETHROUGH),
    /**
     * {@link ChatColor#UNDERLINE} ({@code n})
     */
    UNDERLINE("<underline>", TextDecoration.UNDERLINED, ChatColor.UNDERLINE),
    /**
     * {@link ChatColor#ITALIC} ({@code o})
     */
    ITALIC("<italic>", TextDecoration.ITALIC, ChatColor.ITALIC),
    /**
     * {@link ChatColor#RESET} ({@code r})
     */
    RESET("<reset>", Reset.INSTANCE, ChatColor.RESET);

    /**
     * The MiniMessage tag of the color
     */
    @NotNull public final String miniMessageTag;
    /**
     * The {@link TextFormat} equivalent of the MiniMessage color
     */
    @NotNull public final TextFormat textFormat;
    /**
     * The {@link ChatColor} equivalent of the MiniMessage color
     */
    @NotNull public final ChatColor chatColor;

    MiniColor(@NotNull String miniMessageTag, @NotNull TextFormat textFormat, @NotNull ChatColor chatColor) {
        this.miniMessageTag = miniMessageTag;
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
        return miniMessageTag;
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

    @Nullable
    public static MiniColor fromMiniMessageTag(@NotNull String tag) {
        return Arrays.stream(values())
                .filter(miniColor -> miniColor.miniMessageTag.equals(tag))
                .findFirst()
                .orElse(null);
    }
}
