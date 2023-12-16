package xyz.srnyx.annoyingapi.utility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.entity.RefEntity.*;


/**
 * Utility methods relating to Bukkit
 */
public class BukkitUtility {
    @NotNull private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)&[0-9A-FK-OR]");

    /**
     * Translates {@code &} color codes to {@link ChatColor}
     *
     * @param   object  the object/message to translate
     *
     * @return          the translated object/message
     */
    @NotNull
    public static String color(@Nullable Object object) {
        if (object == null) return "null";
        return ChatColor.translateAlternateColorCodes('&', String.valueOf(object));
    }

    /**
     * Translates {@code &} color codes to {@link ChatColor} for a {@link Collection} of messages
     *
     * @param   objects the objects/messages to translate
     *
     * @return          the translated objects/messages
     */
    @NotNull
    public static List<String> colorCollection(@Nullable Collection<?> objects) {
        if (objects == null) return new ArrayList<>();
        return objects.stream()
                .map(BukkitUtility::color)
                .collect(Collectors.toList());
    }

    /**
     * Strips untranslated {@link ChatColor ChatColors} (using {@code &}) from a {@link String}
     *
     * @param   string  the {@link String} to strip
     *
     * @return          the stripped {@link String}
     */
    @NotNull
    public static String stripUntranslatedColor(@Nullable String string) {
        if (string == null) return "null";
        return STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    /**
     * Toggles a scoreboard tag on an {@link Entity}
     *
     * @param   entity          the {@link Entity} to toggle the scoreboard tag on
     * @param   scoreboardTag   the scoreboard tag to toggle
     *
     * @return                  true if the scoreboard tag was added, false if it was removed (or if it failed)
     */
    public static boolean toggleScoreboardTag(@NotNull Entity entity, @NotNull String scoreboardTag) {
        if (ENTITY_GET_SCOREBOARD_TAGS_METHOD == null || ENTITY_REMOVE_SCOREBOARD_TAG_METHOD == null || ENTITY_ADD_SCOREBOARD_TAG_METHOD == null) return false;
        try {
            if (((Set<String>) ENTITY_GET_SCOREBOARD_TAGS_METHOD.invoke(entity)).contains(scoreboardTag)) {
                ENTITY_REMOVE_SCOREBOARD_TAG_METHOD.invoke(entity, scoreboardTag);
                return false;
            } else {
                ENTITY_ADD_SCOREBOARD_TAG_METHOD.invoke(entity, scoreboardTag);
                return true;
            }
        } catch (final InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets the value of a permission node from a {@link Player}
     * <br><br>
     * <b>Example:</b> {@code player} has {@code friends.max.5} permission, {@code getPermissionValue(player, "friends.max.")} would return {@code 5}
     *
     * @param   player      the {@link Player} to get the permission value from
     * @param   permission  the permission node to get the value of
     *
     * @return              the value of the permission node, or null if not found
     */
    @Nullable
    public static Integer getPermissionValue(@NotNull Player player, @NotNull String permission) {
        for (final PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            final String perm = info.getPermission();
            if (perm.startsWith(permission)) try {
                return Integer.parseInt(perm.substring(permission.length()));
            } catch (final NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Gets an {@link OfflinePlayer} from the specified name
     *
     * @param   name    the name of the player
     *
     * @return          the {@link OfflinePlayer}, or null if not found
     */
    @Nullable
    public static OfflinePlayer getOfflinePlayer(@NotNull String name) {
        for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            final String offlineName = offline.getName();
            if (offlineName != null && offlineName.equalsIgnoreCase(name)) return offline;
        }
        return null;
    }

    /**
     * Gets a {@link Set} of all online player names
     *
     * @return  the {@link Set} of player names
     */
    @NotNull
    public static Set<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of all offline player names
     *
     * @return  the {@link Set} of player names
     */
    @NotNull
    public static Set<String> getOfflinePlayerNames() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> !player.isOnline())
                .map(OfflinePlayer::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of all player names
     *
     * @return  the {@link Set} of player names
     */
    @NotNull
    public static Set<String> getAllPlayerNames() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of all world names
     *
     * @return  the {@link Set} of world names
     */
    @NotNull
    public static Set<String> getWorldNames() {
        return Bukkit.getWorlds().stream()
                .map(org.bukkit.World::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Constructs a new {@link BukkitUtility} instance (illegal)
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private BukkitUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
