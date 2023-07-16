package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.parents.Annoyable;
import xyz.srnyx.annoyingapi.parents.Stringable;
import xyz.srnyx.annoyingapi.utility.DurationFormatUtility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * This class is used to create and manage cooldowns for anything with a {@link UUID}
 */
public class AnnoyingCooldown extends Stringable implements Annoyable {
    @NotNull private final AnnoyingPlugin plugin;
    @NotNull private final UUID uuid;
    /**
     * The {@link CooldownType} that was used to create this cooldown
     */
    @NotNull public final CooldownType type;

    /**
     * Creates and starts a cooldown of the specified {@link CooldownType} for the specified {@link UUID}
     *
     * @param   plugin  the plugin that is creating the cooldown
     * @param   uuid    the UUID of the thing that is being cooled down
     * @param   type    {@link #type}
     */
    public AnnoyingCooldown(@NotNull AnnoyingPlugin plugin, @NotNull UUID uuid, @NotNull CooldownType type) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.type = type;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
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
        final Map<CooldownType, Long> cooldowns = plugin.cooldowns.get(uuid);
        if (cooldowns != null) cooldowns.remove(type);
    }

    /**
     * A {@code long} of the time remaining on the cooldown
     *
     * @return  amount of time left in the cooldown (in milliseconds)
     *
     * @see     DurationFormatUtility#formatDuration(long, String, boolean)
     */
    public long getRemaining() {
        final Map<CooldownType, Long> map = plugin.cooldowns.get(uuid);
        if (map == null) return 0;
        final Long time = map.get(type);
        if (time == null) return 0;
        return time - System.currentTimeMillis();
    }

    /**
     * A {@code boolean} of whether the cooldown is active. If the cooldown is not active, it will be {@link #stop() stopped}
     *
     * @return  whether the cooldown is active
     */
    public boolean isOnCooldown() {
        final boolean onCooldown = getRemaining() > 0;
        if (!onCooldown) stop();
        return onCooldown;
    }

    /**
     * Implement this interface to create your own cooldown types (enums are recommended)
     */
    public interface CooldownType {
        /**
         * Returns the cooldowns duration in milliseconds
         *
         * @return  the duration of the cooldown in milliseconds
         */
        @SuppressWarnings("SameReturnValue")
        long getDuration();
    }
}
