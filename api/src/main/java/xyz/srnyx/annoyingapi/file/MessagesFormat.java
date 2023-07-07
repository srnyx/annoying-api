package xyz.srnyx.annoyingapi.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public enum MessagesFormat {
    LEGACY("Legacy"),
    MINIMESSAGE("MiniMessage");

    @NotNull public final String statsName; // DO NOT CHANGE THE ENUM'S VALUES FOR THIS VARIABLE

    MessagesFormat(@NotNull final String statsName) {
        this.statsName = statsName;
    }

    public boolean isLegacy() {
        return this == LEGACY;
    }

    public boolean isMiniMessage() {
        return this == MINIMESSAGE;
    }

    @NotNull
    public static MessagesFormat fromString(@Nullable String string) {
        if (string == null) return LEGACY;
        try {
            return MessagesFormat.valueOf(string.toUpperCase());
        } catch (final IllegalArgumentException exception) {
            return LEGACY;
        }
    }
}
