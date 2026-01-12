package xyz.srnyx.annoyingapi.reflection.org.bukkit;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;


/**
 * 1.13+ org.bukkit.GameRule
 */
public class RefGameRule {
    /**
     * 1.13+ org.bukkit.GameRule
     */
    @Nullable public static final Class<?> GAME_RULE_CLASS = ReflectionUtility.getClass(1, 13, 0, RefGameRule.class);

    /**
     * 1.13+ org.bukkit.GameRule#KEEP_INVENTORY
     */
    @Nullable public static final Object GAME_RULE_KEEP_INVENTORY = ReflectionUtility.getStaticFieldValue(1, 13, 0, GAME_RULE_CLASS, "KEEP_INVENTORY");

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefGameRule() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
