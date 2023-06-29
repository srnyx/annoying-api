package xyz.srnyx.annoyingapi.utility.adventure;

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
    BLACK(ChatColor.BLACK),
    /**
     * {@link ChatColor#DARK_BLUE} ({@code 1})
     */
    DARK_BLUE(ChatColor.DARK_BLUE),
    /**
     * {@link ChatColor#DARK_GREEN} ({@code 2})
     */
    DARK_GREEN(ChatColor.DARK_GREEN),
    /**
     * {@link ChatColor#DARK_AQUA} ({@code 3})
     */
    DARK_AQUA(ChatColor.DARK_AQUA),
    /**
     * {@link ChatColor#DARK_RED} ({@code 4})
     */
    DARK_RED(ChatColor.DARK_RED),
    /**
     * {@link ChatColor#DARK_PURPLE} ({@code 5})
     */
    DARK_PURPLE(ChatColor.DARK_PURPLE),
    /**
     * {@link ChatColor#GOLD} ({@code 6})
     */
    GOLD(ChatColor.GOLD),
    /**
     * {@link ChatColor#GRAY} ({@code 7})
     */
    GRAY(ChatColor.GRAY),
    /**
     * {@link ChatColor#DARK_GRAY} ({@code 8})
     */
    DARK_GRAY(ChatColor.DARK_GRAY),
    /**
     * {@link ChatColor#BLUE} ({@code 9})
     */
    BLUE(ChatColor.BLUE),
    /**
     * {@link ChatColor#GREEN} ({@code a})
     */
    GREEN(ChatColor.GREEN),
    /**
     * {@link ChatColor#AQUA} ({@code b})
     */
    AQUA(ChatColor.AQUA),
    /**
     * {@link ChatColor#RED} ({@code c})
     */
    RED(ChatColor.RED),
    /**
     * {@link ChatColor#LIGHT_PURPLE} ({@code d})
     */
    LIGHT_PURPLE(ChatColor.LIGHT_PURPLE),
    /**
     * {@link ChatColor#YELLOW} ({@code e})
     */
    YELLOW(ChatColor.YELLOW),
    /**
     * {@link ChatColor#WHITE} ({@code f})
     */
    WHITE(ChatColor.WHITE),
    /**
     * {@link ChatColor#MAGIC} ({@code k})
     */
    MAGIC(ChatColor.MAGIC),
    /**
     * {@link ChatColor#BOLD} ({@code l})
     */
    BOLD(ChatColor.BOLD),
    /**
     * {@link ChatColor#STRIKETHROUGH} ({@code m})
     */
    STRIKETHROUGH(ChatColor.STRIKETHROUGH),
    /**
     * {@link ChatColor#UNDERLINE} ({@code n})
     */
    UNDERLINE(ChatColor.UNDERLINE),
    /**
     * {@link ChatColor#ITALIC} ({@code o})
     */
    ITALIC(ChatColor.ITALIC),
    /**
     * {@link ChatColor#RESET} ({@code r})
     */
    RESET(ChatColor.RESET);

    /**
     * The {@link ChatColor} equivalent of the MiniMessage color
     */
    @NotNull public final ChatColor chatColor;

    MiniColor(@NotNull ChatColor chatColor) {
        this.chatColor = chatColor;
    }

    /**
     * Returns the MiniMessage format of the color
     *
     * @return  the MiniMessage format of the color
     */
    @Override @NotNull
    public String toString() {
        return "<" + name().toLowerCase() + ">";
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
