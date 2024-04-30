package xyz.srnyx.annoyingapi.cooldown;

/**
 * Implement this interface to create your own cooldown types (enums are recommended)
 */
public interface CooldownType {
    /**
     * Returns the cooldowns duration in milliseconds
     *
     * @return the duration of the cooldown in milliseconds
     */
    @SuppressWarnings("SameReturnValue")
    long getDuration();
}
