package xyz.srnyx.annoyingapi.reflection.org.bukkit;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.World
 */
public class RefWorld {
    /**
     * 1.9+ <a href="https://helpch.at/docs/1.9/org/bukkit/World.html#spawnParticle(org.bukkit.Particle,%20org.bukkit.Location,%20int,%20double,%20double,%20double,%20double)">org.bukkit.World#spawnParticle(org.bukkit.Particle, org.bukkit.Location, int, double, double, double, double)</a>
     */
    @Nullable public static final Method WORLD_SPAWN_PARTICLE_METHOD = ReflectionUtility.getMethod(1, 9, 0, World.class, "spawnParticle", RefParticle.PARTICLE_ENUM, Location.class, int.class, double.class, double.class, double.class, double.class);
}
