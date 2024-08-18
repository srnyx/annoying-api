package xyz.srnyx.annoyingapi.reflection.org.bukkit;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.*;


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

    /**
     * Get a {@link PotionEffectType} by name, using reflection if needed
     *
     * @param   name    the name of the potion effect
     *
     * @return          the {@link PotionEffectType}
     */
    @NotNull
    public static Optional<PotionEffectType> getEffect(@NotNull String name) {
        // 1.20.3+
        if (GET_METHOD != null && EFFECT_FIELD != null && MINECRAFT_METHOD != null) try {
            return Optional.ofNullable((PotionEffectType) GET_METHOD.invoke(EFFECT_FIELD, MINECRAFT_METHOD.invoke(null, name)));
        } catch (final IllegalAccessException | InvocationTargetException ignored) {
            // Ignored
        }
        // 1.20.2-
        return Optional.ofNullable(PotionEffectType.getByName(name));
    }

    /**
     * Get an {@link Enchantment} by name, using reflection if needed
     *
     * @param   name    the name of the enchantment
     *
     * @return          the {@link Enchantment}
     */
    @NotNull
    public static Optional<Enchantment> getEnchantment(@NotNull String name) {
        // 1.14+
        if (GET_METHOD != null && ENCHANTMENT_FIELD != null && MINECRAFT_METHOD != null) try {
            return Optional.ofNullable((Enchantment) GET_METHOD.invoke(ENCHANTMENT_FIELD, MINECRAFT_METHOD.invoke(null, name)));
        } catch (final IllegalAccessException | InvocationTargetException ignored) {
            // Ignored
        }
        // 1.13.2-
        return Optional.ofNullable(Enchantment.getByName(name));
    }

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefRegistry() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
