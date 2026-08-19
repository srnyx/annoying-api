package xyz.srnyx.annoyingapi;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.semver4j.Semver;


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

    @NotNull public static final ServerSoftware SOFTWARE;
    @Nullable public static final Semver MINECRAFT_VERSION = Semver.parse(Bukkit.getVersion().split("MC: ")[1].split("\\)")[0]);
    static {
        // Default to SPIGOT
        ServerSoftware newSoftware = SPIGOT;

        // Detect software by class existence
        for (final ServerSoftware software : values()) {
            if (software.possibleClasses != null) {
                for (final String possibleClass : software.possibleClasses) {
                    try {
                        Class.forName(possibleClass);
                        newSoftware = software;
                    } catch (final ClassNotFoundException ignored) {}
                }
            }
        }

        SOFTWARE = newSoftware;
    }
}
