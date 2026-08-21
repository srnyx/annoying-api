package xyz.srnyx.annoyingapi.reflection.org.bukkit;

import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;


/**
 * 1.9+ org.bukkit.Particle
 */
public enum RefParticle {;
    /**
     * 1.9+ org.bukkit.Particle
     */
    @SuppressWarnings("rawtypes")
    @Nullable public static final Class<? extends Enum> PARTICLE_ENUM = ReflectionUtility.getEnum(1, 9, 0, RefParticle.class);
}
