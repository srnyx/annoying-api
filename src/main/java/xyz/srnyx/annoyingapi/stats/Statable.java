package xyz.srnyx.annoyingapi.stats;

import org.jetbrains.annotations.Nullable;

import java.util.Map;


public interface Statable {
    @Nullable
    default Object toStatValue() {
        return toString();
    }

    /**
     * Overrides {@link #toStatValue()} if non-null
     */
    @Nullable
    default Map<String, Object> toStatMap() {
        return null;
    }
}
