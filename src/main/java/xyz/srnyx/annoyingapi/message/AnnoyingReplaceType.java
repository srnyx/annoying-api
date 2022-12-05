package xyz.srnyx.annoyingapi.message;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public enum AnnoyingReplaceType {
    TIME("hh:ss");

    @NotNull private final String defaultCustom;

    @Contract(pure = true)
    AnnoyingReplaceType(@NotNull String defaultCustom) {
        this.defaultCustom = defaultCustom;
    }

    @NotNull @Contract(pure = true)
    public String getDefaultCustom() {
        return defaultCustom;
    }
}
