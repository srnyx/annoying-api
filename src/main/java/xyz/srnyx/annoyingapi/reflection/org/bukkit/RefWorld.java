package xyz.srnyx.annoyingapi.reflection.org.bukkit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;


/**
 * org.bukkit.World
 */
public class RefWorld {
    /**
     * 1.13+ {@code  org.bukkit.World#getGameRuleValue(org.bukkit.GameRule<T>)}
     */
    @Nullable public static final Method WORLD_GET_GAME_RULE_VALUE_METHOD = ReflectionUtility.getMethod(1, 13, 0, World.class, "getGameRuleValue", RefGameRule.GAME_RULE_CLASS);

    /**
     * 1.13+ {@code org.bukkit.World#setGameRuleValue(org.bukkit.GameRule<T>, T)}
     */
    @Nullable public static final Method WORLD_SET_GAME_RULE_VALUE_METHOD = ReflectionUtility.getMethod(1, 13, 0, World.class, "setGameRuleValue", RefGameRule.GAME_RULE_CLASS, Object.class);

    /**
     * 1.11+ org.bukkit.World#playSound(Location, Sound, SoundCategory, float, float)
     */
    @Nullable public static final Method WORLD_PLAY_SOUND_METHOD = ReflectionUtility.getMethod(1, 11, 0, World.class, "playSound", Location.class, Sound.class, RefSoundCategory.SOUND_CATEGORY_ENUM, float.class, float.class);

    /**
     * Gets a game rule value from a world
     *
     * @param   world       the {@link World}
     * @param   legacyName  the legacy name of the game rule for 1.13-
     * @param   modernEnum  the modern enum of the game rule for 1.13+, get it from {@link RefGameRule}
     *
     * @return              the game rule value, or null if not found
     */
    @Nullable
    public static String getGameRuleValue(@NotNull World world, @NotNull String legacyName, @Nullable Object modernEnum) {
        if (WORLD_GET_GAME_RULE_VALUE_METHOD != null && modernEnum != null) try {
            return String.valueOf(WORLD_GET_GAME_RULE_VALUE_METHOD.invoke(world, modernEnum));
        } catch (final IllegalAccessException | InvocationTargetException e) {
            return null;
        }
        return world.getGameRuleValue(legacyName);
    }

    /**
     * Sets a game rule value in a world
     *
     * @param   world       the {@link World}
     * @param   legacyName  the legacy name of the game rule for 1.13-
     * @param   modernEnum  the modern enum of the game rule for 1.13+, get it from {@link RefGameRule}
     * @param   value       the value to set
     */
    public static void setGameRuleValue(@NotNull World world, @NotNull String legacyName, @Nullable Object modernEnum, @NotNull Object value) {
        if (WORLD_SET_GAME_RULE_VALUE_METHOD != null && modernEnum != null) {
            try {
                WORLD_SET_GAME_RULE_VALUE_METHOD.invoke(world, modernEnum, value);
            } catch (final IllegalAccessException | InvocationTargetException ignored) {
                AnnoyingPlugin.log(Level.WARNING, "Failed to set game rule " + legacyName + " to " + value + " in world " + world.getName());
            }
            return;
        }
        world.setGameRuleValue(legacyName, String.valueOf(value));
    }

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefWorld() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
