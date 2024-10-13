package xyz.srnyx.annoyingapi.cooldown;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import xyz.srnyx.javautilities.manipulation.DurationFormatter;
import xyz.srnyx.javautilities.parents.Stringable;


/**
 * This class is used to create and manage cooldowns
 * <br><b>All cooldowns are removed when the server is restarted!</b> <i>They're only stored in {@link CooldownManager#cooldowns}</i>
 */
public class AnnoyingCooldown extends Stringable {
    /**
     * The {@link CooldownManager} that is managing this cooldown
     */
    @NotNull private final CooldownManager manager;
    /**
     * A string indicating the type of cooldown (examples: {@code command.play}, {@code use_ability}, etc...)
     */
    @NotNull public final String type;
    /**
     * The key that "owns" this cooldown
     */
    @NotNull public final String key;
    /**
     * The duration of the cooldown (in milliseconds)
     */
    public final long duration;
    /**
     * The time that this cooldown will expire
     * <br>{@code null} if the cooldown hasn't started
     */
    @Nullable private Long expires;

    /**
     * Creates a new cooldown with the given type, key, and duration
     *
     * @param   manager     {@link #manager}
     * @param   type        {@link #type}
     * @param   key         {@link #key}
     * @param   duration    the duration of the cooldown (in milliseconds)
     */
    public AnnoyingCooldown(@NotNull CooldownManager manager, @NotNull Object type, @NotNull String key, long duration) {
        this.manager = manager;
        this.key = key;
        this.type = type.toString();
        this.duration = duration;
    }

    /**
     * Creates a new cooldown with the given type, key, and duration
     *
     * @param   plugin      the plugin that is creating the cooldown (used to get the {@link #manager})
     * @param   type        {@link #type}
     * @param   key         {@link #key}
     * @param   duration    the duration of the cooldown (in milliseconds)
     */
    public AnnoyingCooldown(@NotNull AnnoyingPlugin plugin, @NotNull Object type, @NotNull String key, long duration) {
        this(plugin.cooldownManager, type, key, duration);
    }

    /**
     * A {@code long} of the time remaining on the cooldown
     *
     * @return  amount of time left in the cooldown (in milliseconds)
     *
     * @see     DurationFormatter#formatDuration(long, String, boolean)
     */
    public long getRemaining() {
        return expires == null ? 0 : expires - System.currentTimeMillis();
    }

    /**
     * Checks if the cooldown is still going
     *
     * @return  whether the cooldown is still going
     */
    public boolean isOnCooldown() {
        return getRemaining() > 0;
    }

    /**
     * Checks if the cooldown is still going
     * <br>If it isn't, it will {@link #stop() stop the cooldown}
     *
     * @return  whether the cooldown is still going
     */
    public boolean isOnCooldownStop() {
        final boolean onCooldown = isOnCooldown();
        if (!onCooldown) stop();
        return onCooldown;
    }

    /**
     * Checks if the cooldown is still going
     * <br>If it isn't, it will {@link #start() start the cooldown}
     *
     * @return  whether the cooldown is still going
     */
    public boolean isOnCooldownStart() {
        final boolean onCooldown = isOnCooldown();
        if (!onCooldown) start();
        return onCooldown;
    }

    /**
     * Starts the cooldown
     * <br>If the cooldown is already started, it will be restarted
     */
    public void start() {
        expires = System.currentTimeMillis() + duration;
        manager.cooldowns.add(this);
    }

    /**
     * Stops the cooldown
     */
    public void stop() {
        expires = null;
        manager.cooldowns.remove(this);
    }

    /**
     * Checks if the given object is equal to this cooldown
     * <br>It is equal if the {@link #key} and {@link #type} are the same
     *
     * @param   other   the object to compare to
     *
     * @return          whether the object is equal to this cooldown
     */
    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) return true;
        if (!(other instanceof AnnoyingCooldown)) return false;
        final AnnoyingCooldown cooldown = (AnnoyingCooldown) other;
        return type.equals(cooldown.type) && key.equals(cooldown.key);
    }

    /**
     * Returns the hash code of this cooldown
     * <br>It is the sum of the hash codes of the {@link #type} and {@link #key}
     *
     * @return  the hash code of this cooldown
     */
    @Override
    public int hashCode() {
        return type.hashCode() + key.hashCode();
    }
}
