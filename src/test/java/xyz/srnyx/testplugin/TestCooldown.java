package xyz.srnyx.testplugin;

import org.jetbrains.annotations.Contract;

import xyz.srnyx.annoyingapi.AnnoyingCooldown;


public enum TestCooldown implements AnnoyingCooldown.CooldownType {
    TEST;

    @Override @Contract(pure = true)
    public long getDuration() {
        return 3000;
    }
}
