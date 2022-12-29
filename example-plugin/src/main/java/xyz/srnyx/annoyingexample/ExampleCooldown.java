package xyz.srnyx.annoyingexample;

import org.jetbrains.annotations.Contract;

import xyz.srnyx.annoyingapi.AnnoyingCooldown;


/**
 * Example of a {@link AnnoyingCooldown.CooldownType} implementation
 */
public enum ExampleCooldown implements AnnoyingCooldown.CooldownType {
    /**
     * Example cooldown
     */
    EXAMPLE;

    @Override @Contract(pure = true)
    public long getDuration() {
        return 3000;
    }
}
