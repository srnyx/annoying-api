package xyz.srnyx.annoyingapi.cooldown;

import com.google.common.collect.ImmutableSet;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import java.util.*;


public class CooldownManager implements Annoyable {
    @NotNull private final AnnoyingPlugin plugin;
    @NotNull public final Map<String, Set<AnnoyingCooldown>> cooldowns = new HashMap<>();

    public CooldownManager(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    @NotNull
    public Set<AnnoyingCooldown> getCooldowns(@NotNull Object key) {
        final Set<AnnoyingCooldown> set = cooldowns.get(key.toString());
        return set == null ? Collections.emptySet() : ImmutableSet.copyOf(set);
    }

    @NotNull
    public AnnoyingCooldown getCooldown(@NotNull Object key, @NotNull CooldownType type) {
        return getCooldowns(key).stream()
                .filter(cooldown -> cooldown.type.equals(type))
                .findFirst()
                .orElse(new AnnoyingCooldown(plugin, key.toString(), type));
    }
}
