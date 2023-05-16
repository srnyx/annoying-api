package xyz.srnyx.annoyingapi;

import com.google.gson.JsonElement;

import org.bukkit.Bukkit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.AnnoyingUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


/**
 * Class for handling update checking
 */
public class AnnoyingUpdate {
    /**
     * The {@link AnnoyingPlugin plugin} instance
     */
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * The current version of the plugin
     */
    @NotNull private final Version currentVersion;
    /**
     * The user agent to use when making requests
     */
    @NotNull private final String userAgent;
    /**
     * The platforms the plugin is available on
     */
    @NotNull private final Map<Platform, String> platforms;
    /**
     * The latest version of the plugin
     */
    @Nullable public final Version latestVersion;

    /**
     * Creates a new {@link AnnoyingUpdate} object
     *
     * @param   plugin          {@link #plugin}
     * @param   platforms       {@link #platforms}
     */
    public AnnoyingUpdate(@NotNull AnnoyingPlugin plugin, @NotNull Map<Platform, String> platforms) {
        this.plugin = plugin;
        this.currentVersion = new Version(plugin.getDescription().getVersion());
        this.userAgent = plugin.getName() + "/" + currentVersion.versionString + " via AnnoyingAPI (update)";
        this.platforms = platforms;
        this.latestVersion = getLatestVersion();
    }

    /**
     * Checks if an update is available and sends {@link AnnoyingOptions#updateAvailable a message} to the console if it is
     *
     * @return  {@code true} if an update is available, {@code false} otherwise
     */
    public boolean checkUpdate() {
        final boolean update = isUpdateAvailable();
        if (update) {
            assert latestVersion != null;
            plugin.log(Level.WARNING, new AnnoyingMessage(plugin, plugin.options.updateAvailable)
                    .replace("%plugin%", plugin.getName())
                    .replace("%current%", plugin.getDescription().getVersion())
                    .replace("%new%", latestVersion.versionString)
                    .toString());
        }
        return update;
    }

    /**
     * Checks if an update is available
     *
     * @return  {@code true} if an update is available, {@code false} otherwise
     */
    public boolean isUpdateAvailable() {
        if (latestVersion == null) return false;
        return latestVersion.value > currentVersion.value;
    }

    @Nullable
    private Version getLatestVersion() {
        // Modrinth
        if (platforms.containsKey(Platform.MODRINTH)) {
            final Version modrinth = modrinth();
            if (modrinth != null) return modrinth;
        }

        // Spigot
        if (platforms.containsKey(Platform.SPIGOT)) return spigot();

        return null;
    }

    /**
     * Checks Modrinth for the latest version
     *
     * @return  the latest version, or {@code null} if an error occurred
     */
    @Nullable
    private Version modrinth() {
        final String[] version = Bukkit.getBukkitVersion().split("\\.");
        final JsonElement json = AnnoyingUtility.getJson(userAgent,
                "https://api.modrinth.com/v2/project/" + platforms.get(Platform.MODRINTH) + "/version" +
                        "?loaders=%5B%22spigot%22,%22paper%22,%22purpur%22%5D" +
                        "&game_versions=%5B%22" + version[0] + "." + version[1] + "." + version[2].split("-")[0] + "%22%5D");

        // Request failed
        if (json == null) {
            platforms.remove(Platform.MODRINTH);
            return getLatestVersion();
        }

        // Return the latest version
        return new Version(json.getAsJsonArray().get(0).getAsJsonObject().get("version_number").getAsString());
    }

    /**
     * Checks Spigot for the latest version
     *
     * @return  the latest version, or {@code null} if an error occurred
     */
    @Nullable
    private Version spigot() {
        final JsonElement json = AnnoyingUtility.getJson(userAgent, "https://api.spiget.org/v2/resources/" + platforms.get(Platform.SPIGOT) + "/versions/latest");

        // Request failed
        if (json == null) {
            platforms.remove(Platform.SPIGOT);
            return getLatestVersion();
        }

        // Return the latest version
        return new Version(json.getAsJsonObject().get("name").getAsString());
    }

    /**
     * Platforms that plugins are available on
     */
    public enum Platform {
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
        SPIGOT
    }

    /**
     * Class for handling versions (only supports numbers [letters/words will be ignored]) to allow for easy comparison
     * <p><b>This will work best if the version is in <a href="https://semver.org">semantic format</a></b>
     */
    public static class Version {
        /**
         * The version as a {@link String}
         */
        @NotNull public final String versionString;
        /**
         * The value of the version
         * <p>Do NOT modify this value
         */
        public int value = 0;

        /**
         * Creates a new {@link Version} object
         *
         * @param   versionString   {@link #versionString}
         */
        public Version(@NotNull String versionString) {
            this.versionString = versionString;

            // Set value
            final List<Integer> values = new ArrayList<>();
            int length = 0;
            for (final String string : versionString.split("\\.")) {
                try {
                    values.add(Integer.parseInt(string));
                } catch (final NumberFormatException e) {
                    break;
                }
                length++;
            }

            for (int i = 0; i < values.size(); i++) value += values.get(0) * Math.pow(10, length - i);
        }
    }
}
