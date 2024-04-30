package xyz.srnyx.annoyingapi.cooldown;

import com.google.common.collect.ImmutableSet;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * This class is used to manage all cooldowns
 *
 * @see AnnoyingPlugin#cooldownManager
 * @see AnnoyingCooldown
 */
public class CooldownManager extends Stringable implements Annoyable {
    /**
     * The {@link AnnoyingPlugin} instance
     */
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * A map of all cooldowns, with the key being the key of the cooldowns
     */
    @NotNull public final Map<String, Set<AnnoyingCooldown>> cooldowns = new HashMap<>();

    /**
     * Creates a new cooldown manager with the given plugin
     *
     * @param   plugin  {@link #plugin}
     */
    public CooldownManager(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    /**
     * Get all cooldowns with the given key
     *
     * @param   key the key of the cooldowns
     *
     * @return      all cooldowns with the given key
     */
    @NotNull
    public Set<AnnoyingCooldown> getCooldowns(@NotNull Object key) {
        final Set<AnnoyingCooldown> set = cooldowns.get(key.toString());
        return set == null ? Collections.emptySet() : ImmutableSet.copyOf(set);
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
    public AnnoyingCooldown getCooldown(@NotNull Object key, @NotNull CooldownType type) {
        return getCooldowns(key).stream()
                .filter(cooldown -> cooldown.type.equals(type))
                .findFirst()
                .orElse(new AnnoyingCooldown(plugin, key.toString(), type));
    }
}
