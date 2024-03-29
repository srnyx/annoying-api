package xyz.srnyx.annoyingapi.reflection.org.bukkit.entity;

import org.bukkit.entity.Entity;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.entity.Entity
 */
public class RefEntity {
    /**
     * 1.10.2+ org.bukkit.entity.Entity#addScoreboardTag(String)
     */
    @Nullable public static final Method ENTITY_ADD_SCOREBOARD_TAG_METHOD = ReflectionUtility.getMethod(1, 10, 2, Entity.class, "addScoreboardTag", String.class);

    /**
     * 1.10.2+ org.bukkit.entity.Entity#getScoreboardTags()
     */
    @Nullable public static final Method ENTITY_GET_SCOREBOARD_TAGS_METHOD = ReflectionUtility.getMethod(1, 10, 2, Entity.class, "getScoreboardTags");

    /**
     * 1.10.2+ org.bukkit.entity.Entity#removeScoreboardTag(String)
     */
    @Nullable public static final Method ENTITY_REMOVE_SCOREBOARD_TAG_METHOD = ReflectionUtility.getMethod(1, 10, 2, Entity.class, "removeScoreboardTag", String.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefEntity() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
