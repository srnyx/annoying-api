package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Represents a Minecraft version
 */
public class MinecraftVersion {
    /**
     * The version string
     */
    @NotNull public final String version;
    /**
     * The major version
     */
    public final int major;
    /**
     * The minor version
     */
    public final int minor;
    /**
     * The patch version
     */
    @Nullable public final Integer patch;
    /**
     * The version as a unique integer
     */
    public final int value;

    /**
     * Creates a new {@link MinecraftVersion}
     *
     * @param   version                     {@link #version}
     *
     * @throws  IllegalArgumentException    if the version does not match the format {@code MAJOR.MINOR} or {@code MAJOR.MINOR.PATCH}
     */
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
        this.value = major * 10000 + minor * 10 + (patch == null ? 0 : patch);
    }
}
