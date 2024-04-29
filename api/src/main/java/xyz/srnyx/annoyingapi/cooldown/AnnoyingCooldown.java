package xyz.srnyx.annoyingapi.cooldown;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import xyz.srnyx.javautilities.manipulation.DurationFormatter;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * This class is used to create and manage cooldowns
 * <br><b>All cooldowns are removed when the server is restarted!</b> <i>They're only stored in {@link CooldownManager#cooldowns}</i>
 */
public class AnnoyingCooldown extends Stringable {
    @NotNull private final CooldownManager manager;
    @NotNull public final String key;
    /**
     * The {@link CooldownType} that was used to create this cooldown
     */
    @NotNull public final CooldownType type;
    private final long time;

    public AnnoyingCooldown(@NotNull AnnoyingPlugin plugin, @NotNull String key, @NotNull CooldownType type) {
        this.manager = plugin.cooldownManager;
        this.key = key;
        this.type = type;
        this.time = System.currentTimeMillis() + type.getDuration();
    }

    /**
     * A {@code long} of the time remaining on the cooldown
     *
     * @return  amount of time left in the cooldown (in milliseconds)
     *
     * @see     DurationFormatter#formatDuration(long, String, boolean)
     */
    public long getRemaining() {
        return time - System.currentTimeMillis();
    }

    /**
     * Whether the cooldown is active, if it is, it will stop the cooldown
     *
     * @return  whether the cooldown is active
     */
    public boolean isOver() {
        final boolean isOver = getRemaining() <= 0;
        if (isOver) stop();
        return isOver;
    }

    public void start() {
        final Set<AnnoyingCooldown> set = manager.cooldowns.get(key);
        if (set == null) {
            manager.cooldowns.put(key, new HashSet<>(Collections.singleton(this)));
            return;
        }
        set.remove(this);
        set.add(this);
    }

    public void stop() {
        final Set<AnnoyingCooldown> set = manager.cooldowns.get(key);
        if (set == null) return;
        set.remove(this);
        if (set.isEmpty()) manager.cooldowns.remove(key);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) return true;
        if (!(other instanceof AnnoyingCooldown)) return false;
        final AnnoyingCooldown cooldown = (AnnoyingCooldown) other;
        return key.equals(cooldown.key) && type.equals(cooldown.type);
    }

    @Override
    public int hashCode() {
        return key.hashCode() + type.hashCode();
    }
}
