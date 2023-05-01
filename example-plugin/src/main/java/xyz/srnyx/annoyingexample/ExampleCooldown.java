package xyz.srnyx.annoyingexample;

import xyz.srnyx.annoyingapi.AnnoyingCooldown;


/**
 * Example of a {@link AnnoyingCooldown.CooldownType} implementation
 * <p>Since there is only one type, we can inline the duration into {@link #getDuration()}
 * <p>Otherwise, we would store it in a field for each type to have its own (unless they all share the same duration), like so:
 * <pre>{@code
 * public enum ExampleCooldown implements AnnoyingCooldown.CooldownType {
 *     EXAMPLE(3000),
 *     ANOTHER_EXAMPLE(5000);
 *
 *     private final long duration;
 *
 *     ExampleCooldown(long duration) {
 *         this.duration = duration;
 *     }
 *
 *     @Override
 *     public long getDuration() {
 *         return duration;
 *     }
 * }
 * }</pre>
 */
public enum ExampleCooldown implements AnnoyingCooldown.CooldownType {
    /**
     * Example cooldown
     */
    EXAMPLE;

    @Override
    public long getDuration() {
        return 3000;
    }
}
