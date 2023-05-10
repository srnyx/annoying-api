package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class MinecraftVersion {
    @NotNull public final String version;
    public final int major;
    public final int minor;
    @Nullable public final Integer patch;
    public final int value;

    public MinecraftVersion(@NotNull String version) {
        this.version = version;
        final String[] split = version.split("\\.");
        try {
            this.major = Integer.parseInt(split[0]);
            this.minor = Integer.parseInt(split[1]);
            this.patch = split.length > 2 ? Integer.parseInt(split[2]) : null;
        } catch (final IndexOutOfBoundsException | NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version: " + version, e);
        }
        this.value = major * 100 + minor * 10 + (patch == null ? 0 : patch);
    }
}
