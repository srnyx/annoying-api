package xyz.srnyx.annoyingapi.reflection.org.bukkit.potion;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;


/**
 * org.bukkit.potion.PotionEffect
 */
public class RefPotionEffect {
    /**
     * 1.13+ org.bukkit.PotionEffect(PotionEffectType, int, int, boolean, boolean, boolean)
     */
    @Nullable public static final Constructor<PotionEffect> POTION_EFFECT_CONSTRUCTOR_6 = ReflectionUtility.getConstructor(1, 13, 0, PotionEffect.class, PotionEffectType.class, int.class, int.class, boolean.class, boolean.class, boolean.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefPotionEffect() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
