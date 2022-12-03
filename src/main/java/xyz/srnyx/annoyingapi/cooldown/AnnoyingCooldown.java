package xyz.srnyx.annoyingapi.cooldown;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * This class is used to create and manage cooldowns for players
 */
public class AnnoyingCooldown {
    /**
     * Stores the cooldowns for each player/type
     */
    @NotNull public static final Map<UUID, Map<AnnoyingCooldownType, Long>> COOLDOWNS = new HashMap<>();

    @NotNull private final UUID uuid;
    @NotNull private final AnnoyingCooldownType type;

    /**
     * Creates and starts a cooldown of the specified {@link AnnoyingCooldownType} for the specified player
     *
     * @param   uuid    the player's UUID
     * @param   type    the cooldown type
     */
    @Contract(pure = true)
    public AnnoyingCooldown(@NotNull UUID uuid, @NotNull AnnoyingCooldownType type) {
        this.uuid = uuid;
        this.type = type;
        COOLDOWNS.computeIfAbsent(uuid, k -> Collections.emptyMap()).put(type, System.currentTimeMillis() + type.getDuration());
    }

    /**
     * @return  amount of time left in the cooldown (in milliseconds)
     */
    public long getRemaining() {
        final Map<AnnoyingCooldownType, Long> map = COOLDOWNS.get(uuid);
        if (map == null) return 0;
        final Long time = map.get(type);
        if (time == null) return 0;
        return time - System.currentTimeMillis();
    }

    /**
     * @param   pattern the pattern to format the time with
     *
     * @return          the formatted time left in the cooldown
     */
    public String getRemainingPretty(@Nullable String pattern) {
        return AnnoyingPlugin.formatMillis(getRemaining(), pattern);
    }

    /**
     * @return  how long the cooldown is (in milliseconds)
     */
    public long getDuration() {
        return type.getDuration();
    }

    /**
     * @return  whether the player is on cooldown
     */
    public boolean isOnCooldown() {
        return getRemaining() > 0;
    }

    /**
     * If the player should no longer be on cooldown, this will remove them from {@link #COOLDOWNS}
     */
    public void check() {
        if (!isOnCooldown()) COOLDOWNS.get(uuid).remove(type);
    }
}
