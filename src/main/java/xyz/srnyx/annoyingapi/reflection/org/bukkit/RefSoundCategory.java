package xyz.srnyx.annoyingapi.reflection.org.bukkit;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;


/**
 * org.bukkit.SoundCategory
 */
public enum RefSoundCategory {;
    /**
     * 1.11+ org.bukkit.SoundCategory
     */
    @SuppressWarnings("rawtypes")
    @Nullable public static final Class<? extends Enum> SOUND_CATEGORY_ENUM = ReflectionUtility.getEnum(1, 11, 0, RefSoundCategory.class);
}
