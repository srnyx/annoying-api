package xyz.srnyx.annoyingapi.utility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import xyz.srnyx.javautilities.manipulation.Mapper;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.entity.RefEntity.*;


/**
 * Utility methods relating to Bukkit
 */
public class BukkitUtility {
    @NotNull private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)[&§][0-9A-FK-OR]");

    /**
     * Translates {@code &} color codes to {@link ChatColor}
     *
     * @param   object  the object/string to translate
     *
     * @return          the translated object/string
     */
    @NotNull
    public static String color(@Nullable Object object) {
        return ChatColor.translateAlternateColorCodes('&', String.valueOf(object));
    }

    /**
     * Translates {@code &} color codes to {@link ChatColor} for multiple strings
     *
     * @param   object1 the first object/string to translate
     * @param   objects the other objects/strings to translate
     *
     * @return          the translated objects/strings
     */
    @NotNull
    public static List<String> color(@Nullable Object object1, @Nullable Object... objects) {
        final List<String> list = new ArrayList<>();
        list.add(color(object1));
        for (final Object object : objects) list.add(color(object));
        return list;
    }

    /**
     * Translates {@code &} color codes to {@link ChatColor} for a {@link Collection} of strings
     *
     * @param   objects the objects/strings to translate
     *
     * @return          the translated objects/strings
     */
    @NotNull
    public static List<String> colorCollection(@Nullable Collection<?> objects) {
        if (objects == null) return new ArrayList<>();
        final List<String> list = new ArrayList<>();
        for (final Object object : objects) list.add(color(object));
        return list;
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
        return string == null ? "null" : STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    /**
     * Translates hex color codes to {@code &x} color codes
     *
     * @param   hex the hex color code
     *
     * @return      the translated hex color code
     */
    @NotNull
    public static String hexColor(@NotNull String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        final StringBuilder builder = new StringBuilder("&x");
        for (final char character : hex.toCharArray()) builder.append('&').append(character);
        return builder.toString();
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
            //noinspection unchecked
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
     * @return              the value of the permission node, or empty if not found
     */
    @NotNull
    public static Optional<Long> getPermissionValue(@NotNull Player player, @NotNull String permission) {
        for (final PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) continue;
            final String perm = info.getPermission();
            if (!perm.startsWith(permission)) continue;
            final Optional<Long> value = Mapper.toLong(perm.substring(permission.length()));
            if (!value.isPresent()) AnnoyingPlugin.log(Level.WARNING, "&cInvalid permission value for &4" + player.getName() + "&c: &4" + perm);
            return value;
        }
        return Optional.empty();
    }

    /**
     * Gets an {@link OfflinePlayer} from the specified name
     * <br>Returns a {@link Player} if they're online
     *
     * @param   name    the name of the player
     *
     * @return          the {@link OfflinePlayer}, or empty if not found
     */
    @NotNull
    public static Optional<OfflinePlayer> getOfflinePlayer(@NotNull String name) {
        // Check online players
        final Player online = Bukkit.getPlayerExact(name);
        if (online != null) return Optional.of(online);

        // Check offline players
        final String nameLower = name.toLowerCase();
        for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            final String offlineName = offline.getName();
            if (offlineName != null && offlineName.toLowerCase().equals(nameLower)) return Optional.of(offline);
        }
        return Optional.empty();
    }

    /**
     * Gets a {@link Set} of all online player names
     *
     * @return  the {@link Set} of player names
     */
    @NotNull
    public static Set<String> getOnlinePlayerNames() {
        final Set<String> set = new HashSet<>();
        for (final Player player : Bukkit.getOnlinePlayers()) set.add(player.getName());
        return set;
    }

    /**
     * Gets a {@link Set} of all offline player names
     *
     * @return  the {@link Set} of player names
     */
    @NotNull
    public static Set<String> getOfflinePlayerNames() {
        final Set<String> set = new HashSet<>();
        for (final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.isOnline()) continue;
            final String name = player.getName();
            if (name != null) set.add(name);
        }
        return set;
    }

    /**
     * Gets a {@link Set} of all player names
     *
     * @return  the {@link Set} of player names
     */
    @NotNull
    public static Set<String> getAllPlayerNames() {
        final Set<String> set = new HashSet<>();
        for (final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            final String name = player.getName();
            if (name != null) set.add(name);
        }
        return set;
    }

    /**
     * Gets a {@link Set} of all world names
     *
     * @return  the {@link Set} of world names
     */
    @NotNull
    public static Set<String> getWorldNames() {
        final Set<String> set = new HashSet<>();
        for (final World world : Bukkit.getWorlds()) set.add(world.getName());
        return set;
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
