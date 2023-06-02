package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Represents a Minecraft version
 */
public class SemanticVersion implements Comparable<SemanticVersion> {
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
    public final int patch;

    /**
     * Creates a new {@link SemanticVersion}
     *
     * @param   major   {@link #major}
     * @param   minor   {@link #minor}
     * @param   patch   {@link #patch}
     */
    public SemanticVersion(@Nullable Integer major, @Nullable Integer minor, @Nullable Integer patch) {
        this.version = major + "." + minor + "." + patch;
        this.major = major == null ? 0 : major;
        this.minor = minor == null ? 0 : minor;
        this.patch = patch == null ? 0 : patch;
    }

    /**
     * Creates a new {@link SemanticVersion}
     *
     * @param   version                     {@link #version}
     *
     * @throws  IllegalArgumentException    if the version does not match the format {@code MAJOR.MINOR} or {@code MAJOR.MINOR.PATCH}
     */
    public SemanticVersion(@NotNull String version) throws IllegalArgumentException {
        this.version = version;
        final String[] split = version.split("\\.");
        try {
            this.major = Integer.parseInt(split[0]);
            this.minor = Integer.parseInt(split[1]);
            this.patch = split.length > 2 ? Integer.parseInt(split[2]) : 0;
        } catch (final IndexOutOfBoundsException | NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version: " + version, e);
        }
    }

    @Override
    public String toString() {
        return version;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other instanceof SemanticVersion) return compareTo((SemanticVersion) other) == 0;
        return false;
    }

    @Override
    public int hashCode() {
        return version.hashCode();
    }

    @Override
    public int compareTo(@NotNull SemanticVersion other) {
        if (major != other.major) return major - other.major;
        if (minor != other.minor) return minor - other.minor;
        return patch - other.patch;
    }

    /**
     * Compares the version to the provided version
     *
     * @param   major   the major version
     * @param   minor   the minor version
     * @param   patch   the patch version
     *
     * @return          0 if the versions are equal, a negative integer if the version is lower, or a positive integer if the version is higher
     */
    public int compareTo(@Nullable Integer major, @Nullable Integer minor, @Nullable Integer patch) {
        return compareTo(new SemanticVersion(major, minor, patch));
    }

    /**
     * Checks if the version is greater than the provided version
     *
     * @param   other   the other version
     *
     * @return          {@code true} if the version is greater than the provided version, otherwise {@code false}
     */
    public boolean isGreaterThan(@NotNull SemanticVersion other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if the version is greater than the provided version
     *
     * @param   major   the major version
     * @param   minor   the minor version
     * @param   patch   the patch version
     *
     * @return          {@code true} if the version is greater than the provided version, otherwise {@code false}
     */
    public boolean isGreaterThan(@Nullable Integer major, @Nullable Integer minor, @Nullable Integer patch) {
        return compareTo(major, minor, patch) > 0;
    }

    /**
     * Checks if the version is greater or equal than the provided version
     *
     * @param   other   the other version
     *
     * @return          {@code true} if the version is greater or equal than the provided version, otherwise {@code false}
     */
    public boolean isGreaterThanOrEqualTo(@NotNull SemanticVersion other) {
        return compareTo(other) >= 0;
    }

    /**
     * Checks if the version is greater or equal than the provided version
     *
     * @param   major   the major version
     * @param   minor   the minor version
     * @param   patch   the patch version
     *
     * @return          {@code true} if the version is greater or equal than the provided version, otherwise {@code false}
     */
    public boolean isGreaterThanOrEqualTo(@Nullable Integer major, @Nullable Integer minor, @Nullable Integer patch) {
        return compareTo(major, minor, patch) >= 0;
    }

    /**
     * Checks if the version is less than the provided version
     *
     * @param   other   the other version
     *
     * @return          {@code true} if the version is less than the provided version, otherwise {@code false}
     */
    public boolean isLessThan(@NotNull SemanticVersion other) {
        return compareTo(other) < 0;
    }

    /**
     * Checks if the version is less than the provided version
     *
     * @param   major   the major version
     * @param   minor   the minor version
     * @param   patch   the patch version
     *
     * @return          {@code true} if the version is less than the provided version, otherwise {@code false}
     */
    public boolean isLessThan(@Nullable Integer major, @Nullable Integer minor, @Nullable Integer patch) {
        return compareTo(major, minor, patch) < 0;
    }

    /**
     * Checks if the version is less or equal than the provided version
     *
     * @param   other   the other version
     *
     * @return          {@code true} if the version is less or equal than the provided version, otherwise {@code false}
     */
    public boolean isLessThanOrEqualTo(@NotNull SemanticVersion other) {
        return compareTo(other) <= 0;
    }

    /**
     * Checks if the version is less or equal than the provided version
     *
     * @param   major   the major version
     * @param   minor   the minor version
     * @param   patch   the patch version
     *
     * @return          {@code true} if the version is less or equal than the provided version, otherwise {@code false}
     */
    public boolean isLessThanOrEqualTo(@Nullable Integer major, @Nullable Integer minor, @Nullable Integer patch) {
        return compareTo(major, minor, patch) <= 0;
    }
}
