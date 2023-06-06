package xyz.srnyx.annoyingapi.reflection.org.bukkit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.World
 */
public class RefWorld {
    /**
     * 1.11+ org.bukkit.World#playSound(Location, Sound, SoundCategory, float, float)
     */
    @Nullable public static final Method WORLD_PLAY_SOUND_METHOD = ReflectionUtility.getMethod(1, 11, 0, World.class, "playSound", Location.class, Sound.class, RefSoundCategory.SOUND_CATEGORY_ENUM, float.class, float.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefWorld() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
