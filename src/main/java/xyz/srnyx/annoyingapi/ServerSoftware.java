package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Get the server software being used based on class existence
 */
public enum ServerSoftware {
    FOLIA("io.papermc.paper.threadedregions.RegionizedServer"),
    PAPER("com.destroystokyo.paper.event.block.BeaconEffectEvent", "org.github.paperspigot.event.block.BeaconEffectEvent"),
    SPIGOT;

    private final @NotNull String @Nullable [] possibleClasses;

    ServerSoftware(@NotNull String @Nullable ... possibleClasses) {
        this.possibleClasses = possibleClasses;
    }

    ServerSoftware() {
        this((String[]) null);
    }

    /**
     * @return  whether this server has Folia classes
     */
    public boolean hasFolia() {
        return this == FOLIA;
    }

    /**
     * @return  whether this server has Paper classes
     */
    public boolean hasPaper() {
        return this == PAPER || hasFolia();
    }

    /**
     * @return  whether this server has Spigot classes
     */
    public boolean hasSpigot() {
        return this == SPIGOT || hasPaper();
    }

    public boolean has(@NotNull ServerSoftware software) {
        return switch (software) {
            case FOLIA -> hasFolia();
            case PAPER -> hasPaper();
            case SPIGOT -> hasSpigot();
        };
    }

    @NotNull
    public static ServerSoftware get() {
        // Detect software by class existence
        for (final ServerSoftware software : values()) {
            if (software.possibleClasses != null) {
                for (final String possibleClass : software.possibleClasses) {
                    try {
                        Class.forName(possibleClass);
                        return software;
                    } catch (final ClassNotFoundException ignored) {}
                }
            }
        }

        // Default to SPIGOT
        return SPIGOT;
    }
}
