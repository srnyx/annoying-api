package xyz.srnyx.annoyingexample;

import org.jetbrains.annotations.Contract;

import xyz.srnyx.annoyingapi.AnnoyingCooldown;


public enum ExampleCooldown implements AnnoyingCooldown.CooldownType {
    EXAMPLE;

    @Override @Contract(pure = true)
    public long getDuration() {
        return 3000;
    }
}
