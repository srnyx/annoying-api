package xyz.srnyx.annoyingapi.cooldown;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This class is used to manage all cooldowns
 *
 * @see AnnoyingPlugin#cooldownManager
 * @see AnnoyingCooldown
 */
public class CooldownManager extends Stringable {
    /**
     * A set of all cooldowns
     */
    @NotNull public final Set<AnnoyingCooldown> cooldowns = new HashSet<>();

    /**
     * Get all cooldowns with the given type
     *
     * @param   type    the type of the cooldowns
     *
     * @return          all cooldowns with the given type
     */
    @NotNull
    public Set<AnnoyingCooldown> getCooldownsByType(@NotNull Object type) {
        final String typeString = type.toString();
        return cooldowns.stream()
                .filter(cooldown -> cooldown.type.equals(typeString))
                .collect(Collectors.toSet());
    }

    /**
     * Get all cooldowns with the given key
     *
     * @param   key the key of the cooldowns
     *
     * @return      all cooldowns with the given key
     */
    @NotNull
    public Set<AnnoyingCooldown> getCooldownsByKey(@NotNull Object key) {
        final String keyString = key.toString();
        return cooldowns.stream()
                .filter(cooldown -> cooldown.key.equals(keyString))
                .collect(Collectors.toSet());
    }

    /**
     * Get a cooldown with the given key and type
     *
     * @param   key     the key of the cooldown
     * @param   type    the type of the cooldown
     *
     * @return          the cooldown with the given key and type
     */
    @NotNull
    public Optional<AnnoyingCooldown> getCooldown(@NotNull Object key, @NotNull Object type) {
        final String keyString = key.toString();
        final String typeString = type.toString();
        return cooldowns.stream()
                .filter(cooldown -> cooldown.key.equals(keyString) && cooldown.type.equals(typeString))
                .findAny();
    }

    /**
     * Get a cooldown with the given key and type
     * <br>If the cooldown doesn't exist yet, it will create a new one with the given key and type (it won't start it though)
     *
     * @param   key     the key of the cooldown
     * @param   type    the type of the cooldown
     *
     * @return          the cooldown with the given key and type or the newly created cooldown
     */
    @NotNull
    public AnnoyingCooldown getCooldownElseNew(@NotNull Object key, @NotNull Object type) {
        return getCooldown(key, type).orElse(new AnnoyingCooldown(this, type, key.toString()));
    }
}
