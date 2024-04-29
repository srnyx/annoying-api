package xyz.srnyx.annoyingexample;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.cooldown.CooldownType;


/**
 * Example of a {@link CooldownType} implementation
 * <p>Since there is only one type with one duration, we can just have it be a class
 * <p>Otherwise, we would make it an enum and store the duration in a field for each type to have its own (unless they all share the same duration), like so:
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
 *     //@Override (uncomment)
 *     public long getDuration() {
 *         return duration;
 *     }
 * }
 * }</pre>
 */
public class ExampleCooldown implements CooldownType {
    /**
     * This is practically the same as a single enum
     */
    @NotNull public static final ExampleCooldown INSTANCE = new ExampleCooldown();

    /**
     * {@link ExampleCooldown}
     */
    public ExampleCooldown() {
        // Only exists to give the constructor a Javadoc
    }

    @Override
    public long getDuration() {
        return 3000;
    }
}
