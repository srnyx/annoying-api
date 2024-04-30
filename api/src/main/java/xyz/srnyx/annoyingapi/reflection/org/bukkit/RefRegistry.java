package xyz.srnyx.annoyingapi.reflection.org.bukkit;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;


/**
 * org.bukkit.Registry
 */
public class RefRegistry {
    /**
     * 1.14+ org.bukkit.Registry
     */
    @Nullable public static final Class<?> REGISTRY_CLASS = ReflectionUtility.getClass(1, 14, 0, RefRegistry.class);
    /**
     * 1.14+ org.bukkit.Registry#get(NamespacedKey)
     */
    @Nullable public static final Method GET_METHOD = ReflectionUtility.getMethod(1, 14, 0, REGISTRY_CLASS, "get", NAMESPACED_KEY_CLASS);
    /**
     * 1.20.3+ org.bukkit.Registry#EFFECT
     */
    @Nullable public static final Object EFFECT_FIELD = ReflectionUtility.getStaticFieldValue(1, 20, 3, REGISTRY_CLASS, "EFFECT");
    /**
     * 1.14+ org.bukkit.Registry#ENCHANTMENT
     */
    @Nullable public static final Object ENCHANTMENT_FIELD = ReflectionUtility.getStaticFieldValue(1, 14, 0, REGISTRY_CLASS, "ENCHANTMENT");

    @Nullable
    public static PotionEffectType getEffect(@NotNull AnnoyingPlugin plugin, @NotNull String name) {
        // 1.20.3+
        if (GET_METHOD != null && EFFECT_FIELD != null && NAMESPACED_KEY_CONSTRUCTOR != null) try {
            return (PotionEffectType) GET_METHOD.invoke(EFFECT_FIELD, NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, name));
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
        // 1.20.2-
        return PotionEffectType.getByName(name);
    }

    @Nullable
    public static Enchantment getEnchantment(@NotNull AnnoyingPlugin plugin, @NotNull String name) {
        // 1.14+
        if (GET_METHOD != null && ENCHANTMENT_FIELD != null && NAMESPACED_KEY_CONSTRUCTOR != null) try {
            return (Enchantment) GET_METHOD.invoke(ENCHANTMENT_FIELD, NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, name));
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
        // 1.13.2-
        return Enchantment.getByName(name);
    }
}
