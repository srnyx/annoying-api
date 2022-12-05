package xyz.srnyx.annoyingapi.cooldown;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.AnnoyingUtility;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * This class is used to create and manage cooldowns for players
 */
public class AnnoyingCooldown {
    @NotNull private final AnnoyingPlugin plugin;
    @NotNull private final UUID uuid;
    @NotNull private final AnnoyingCooldownType type;

    /**
     * Creates and starts a cooldown of the specified {@link AnnoyingCooldownType} for the specified player
     *
     * @param   plugin  the plugin that is creating the cooldown
     * @param   uuid    the player's UUID
     * @param   type    the cooldown type
     */
    @Contract(pure = true)
    public AnnoyingCooldown(@NotNull AnnoyingPlugin plugin, @NotNull UUID uuid, @NotNull AnnoyingCooldownType type) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.type = type;
    }

    /**
     * Starts the cooldown
     */
    public void start() {
        plugin.cooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(type, System.currentTimeMillis() + type.getDuration());
    }

    /**
     * Stops the cooldown
     */
    public void stop() {
        final Map<AnnoyingCooldownType, Long> cooldowns = plugin.cooldowns.get(uuid);
        if (cooldowns != null) cooldowns.remove(type);
    }

    /**
     * A {@code long} of the time remaining on the cooldown
     *
     * @return  amount of time left in the cooldown (in milliseconds)
     *
     * @see     AnnoyingCooldown#getRemainingPretty(String)
     */
    public long getRemaining() {
        final Map<AnnoyingCooldownType, Long> map = plugin.cooldowns.get(uuid);
        if (map == null) return 0;
        final Long time = map.get(type);
        if (time == null) return 0;
        return time - System.currentTimeMillis();
    }

    /**
     * {@link #getRemaining()} but pretty
     *
     * @param   pattern the pattern to format the time with
     *
     * @return          the formatted time left in the cooldown
     *
     * @see             AnnoyingCooldown#getRemaining()
     */
    public String getRemainingPretty(@Nullable String pattern) {
        return AnnoyingUtility.formatMillis(new Date(getRemaining()), pattern);
    }

    /**
     * The duration of the {@link AnnoyingCooldownType}
     *
     * @return  how long the cooldown is (in milliseconds)
     *
     * @see     AnnoyingCooldownType#getDuration()
     */
    public long getDuration() {
        return type.getDuration();
    }

    /**
     * A {@code boolean} of whether the cooldown is active
     *
     * @return  whether the player is on cooldown
     */
    public boolean isOnCooldown() {
        return getRemaining() > 0;
    }

    /**
     * If the player should no longer be on cooldown, this will remove them from {@link AnnoyingPlugin#cooldowns}
     */
    public void check() {
        if (!isOnCooldown()) stop();
    }
}
