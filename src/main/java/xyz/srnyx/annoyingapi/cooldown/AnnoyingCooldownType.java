package xyz.srnyx.annoyingapi.cooldown;


/**
 * Implement this interface to create your own cooldown types
 */
public interface AnnoyingCooldownType {
    /**
     * @return  the duration of the cooldown in milliseconds
     */
    long getDuration();
}
