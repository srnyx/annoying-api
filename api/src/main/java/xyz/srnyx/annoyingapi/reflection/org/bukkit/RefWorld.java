package xyz.srnyx.annoyingapi.reflection.org.bukkit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


public class RefWorld {
    @Nullable public static final Method WORLD_PLAY_SOUND_METHOD = ReflectionUtility.getMethod(1, 11, 0, World.class, "playSound", Location.class, Sound.class, RefSoundCategory.SOUND_CATEGORY_ENUM, float.class, float.class);
}
