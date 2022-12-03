package xyz.srnyx.annoyingapi.download;


/**
 * Platforms that plugins can be downloaded from
 */
public enum AnnoyingPlatform {
    /**
     * <a href="https://modrinth.com">{@code https://modrinth.com}</a>
     * <p>The project's ID <b>or</b> slug
     * <p><b>Example:</b> {@code lzjYdd5h} <i>or</i> {@code personal-phantoms}
     */
    MODRINTH,
    /**
     * <a href="https://spigotmc.org">{@code https://spigotmc.org}</a>
     * <p>The project's ID
     * <p><b>Example:</b> {@code 106381}
     */
    SPIGOT,
    /**
     * <a href="https://dev.bukkit.org">{@code https://dev.bukkit.org/projects}</a>
     * <p>The project's ID <b>or</b> slug
     */
    BUKKIT,
    /**
     * An external direct-download URL
     */
    EXTERNAL,
    /**
     * A URL that the user can manually download the plugin from
     */
    MANUAL
}
