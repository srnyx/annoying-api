package xyz.srnyx.annoyingapi.reflection.org.bukkit.entity;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefSoundCategory;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.entity.Player
 */
public class RefPlayer {
    /**
     * 1.11+ org.bukkit.entity.Player#playSound(Location, Sound, SoundCategory, float, float)
     */
    @Nullable public static final Method PLAYER_PLAY_SOUND_METHOD = ReflectionUtility.getMethod(1, 11, 0, Player.class, "playSound", Location.class, Sound.class, RefSoundCategory.SOUND_CATEGORY_ENUM, float.class, float.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefPlayer() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
