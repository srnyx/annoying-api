package xyz.srnyx.annoyingapi;

/**
 * Platforms that plugins can be downloaded (or checked for updates) from
 */
public enum PluginPlatform {
    /**
     * <a href="https://modrinth.com/plugins">{@code https://modrinth.com/plugins}</a>
     * <p>Project ID <i>or</i> slug
     * <p><b>Example:</b> {@code gzktm9GG} <i>or</i> {@code annoying-api}
     */
    MODRINTH,
    /**
     * <a href="https://spigotmc.org/resources">{@code https://spigotmc.org/resources}</a>
     * <p>Project ID
     * <p><b>Example:</b> {@code 106637}
     */
    SPIGOT,
    /**
     * <a href="https://dev.bukkit.org/projects">{@code https://dev.bukkit.org/projects}</a>
     * <p>Project ID <i>or</i> slug
     * <p><b>Example:</b> {@code 728930} <i>or</i> {@code annoying-api}
     */
    BUKKIT,
    /**
     * An external direct-download URL
     * <p><b>Example:</b> {@code https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib.jar}
     */
    EXTERNAL,
    /**
     * A URL that the user can manually download the plugin from
     * <p><b>Example:</b> {@code https://github.com/srnyx/annoying-api/releases/latest}
     */
    MANUAL
}
