package xyz.srnyx.annoyingapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.AnnoyingUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


/**
 * Class for handling update checking
 */
public class AnnoyingUpdate {
    /**
     * The {@link AnnoyingPlugin plugin} instance
     */
    @NotNull private final AnnoyingPlugin annoyingPlugin;
    /**
     * The {@link JavaPlugin plugin} to check for updates
     */
    @NotNull private final JavaPlugin plugin;
    /**
     * The current version of the plugin
     */
    @NotNull private final Version currentVersion;
    /**
     * The user agent to use when making requests
     */
    @NotNull private final String userAgent;
    /**
     * The platforms the plugin is available on. Currently only {@link PluginPlatform.Platform#MODRINTH} and {@link PluginPlatform.Platform#SPIGOT} are supported
     */
    @NotNull private final PluginPlatform.Multi platforms;
    /**
     * The latest version of the plugin
     */
    @Nullable public final Version latestVersion;

    /**
     * Creates a new {@link AnnoyingUpdate} object
     *
     * @param   annoyingPlugin  {@link #annoyingPlugin}
     * @param   plugin          {@link #plugin}
     * @param   platforms       {@link #platforms}
     */
    public AnnoyingUpdate(@NotNull AnnoyingPlugin annoyingPlugin, @NotNull JavaPlugin plugin, @NotNull PluginPlatform.Multi platforms) {
        this.annoyingPlugin = annoyingPlugin;
        this.plugin = plugin;
        this.currentVersion = new Version(plugin.getDescription().getVersion());
        this.userAgent = annoyingPlugin.getName() + "/" + annoyingPlugin.getDescription().getVersion() + " via AnnoyingAPI (update)";
        this.platforms = platforms;
        this.latestVersion = getLatestVersion();
    }

    /**
     * Creates a new {@link AnnoyingUpdate} object
     *
     * @param   plugin      {@link #annoyingPlugin} and {@link #plugin}
     * @param   platforms   {@link #platforms}
     */
    public AnnoyingUpdate(@NotNull AnnoyingPlugin plugin, @NotNull PluginPlatform.Multi platforms) {
        this(plugin, plugin, platforms);
    }

    /**
     * Checks if an update is available and sends {@link AnnoyingOptions#updateAvailable a message} to the console if it is
     *
     * @return  {@code true} if an update is available, {@code false} otherwise
     */
    public boolean checkUpdate() {
        final boolean update = isUpdateAvailable();
        if (update && latestVersion != null) annoyingPlugin.log(Level.WARNING, new AnnoyingMessage(annoyingPlugin, annoyingPlugin.options.updateAvailable)
                .replace("%plugin%", plugin.getName())
                .replace("%current%", currentVersion.string)
                .replace("%new%", latestVersion.string)
                .toString());
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
        final String modrinthIdentifier = platforms.getIdentifier(PluginPlatform.Platform.MODRINTH);
        if (modrinthIdentifier != null) {
            final Version modrinth = modrinth(modrinthIdentifier);
            if (modrinth != null) return modrinth;
        }

        // Hangar
        final PluginPlatform hangarPlatform = platforms.get(PluginPlatform.Platform.HANGAR);
        if (hangarPlatform != null) {
            final Version hangar = hangar(hangarPlatform);
            if (hangar != null) return hangar;
        }

        // Spigot
        final String spigotIdentifier = platforms.getIdentifier(PluginPlatform.Platform.SPIGOT);
        if (spigotIdentifier != null) return spigot(spigotIdentifier);

        return null;
    }

    /**
     * Checks Modrinth for the latest version
     *
     * @param   identifier  the identifier of the plugin on Modrinth
     *
     * @return              the latest version, or {@code null} if an error occurred
     */
    @Nullable
    private Version modrinth(@NotNull String identifier) {
        final JsonElement json = AnnoyingUtility.getJson(userAgent,
                "https://api.modrinth.com/v2/project/" + identifier + "/version" +
                        "?loaders=%5B%22spigot%22,%22paper%22,%22purpur%22%5D" +
                        "&game_versions=%5B%22" + AnnoyingPlugin.MINECRAFT_VERSION.version + "%22%5D");

        // Request failed
        if (json == null) return fail(PluginPlatform.Platform.MODRINTH);

        // Return the latest version
        try {
            final JsonArray versions = json.getAsJsonArray();
            if (versions.size() == 0) return fail(PluginPlatform.Platform.MODRINTH);
            return new Version(versions.get(0).getAsJsonObject().get("version_number").getAsString());
        } catch (final IllegalStateException e) {
            return fail(PluginPlatform.Platform.MODRINTH);
        }
    }

    /**
     * Checks Hangar for the latest version
     *
     * @param   platform    the {@link PluginPlatform} information
     *
     * @return              the latest version, or {@code null} if an error occurred
     */
    @Nullable
    private Version hangar(@NotNull PluginPlatform platform) {
        final JsonElement json = AnnoyingUtility.getJson(userAgent, "https://hangar.papermc.io/api/v1/projects/" + platform.author + "/" + platform.identifier + "/versions?limit=1&offset=0&platform=PAPER&platformVersion=" + AnnoyingPlugin.MINECRAFT_VERSION.version);

        // Request failed
        if (json == null) return fail(PluginPlatform.Platform.HANGAR);

        // Return the latest version
        return new Version(json.getAsJsonObject().get("result").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());
    }

    /**
     * Checks Spigot for the latest version
     *
     * @param   identifier  the identifier of the plugin on Spigot
     *
     * @return              the latest version, or {@code null} if an error occurred
     */
    @Nullable
    private Version spigot(@NotNull String identifier) {
        final JsonElement json = AnnoyingUtility.getJson(userAgent, "https://api.spiget.org/v2/resources/" + identifier + "/versions/latest");

        // Request failed
        if (json == null) return fail(PluginPlatform.Platform.SPIGOT);

        // Return the latest version
        return new Version(json.getAsJsonObject().get("name").getAsString());
    }

    /**
     * Remove the failed platform from the list of platforms and retry {@link #getLatestVersion() getting the latest version}
     *
     * @param   platform    the platform that failed
     *
     * @return              the latest version, or {@code null} if an error occurred
     */
    @Nullable
    private Version fail(@NotNull PluginPlatform.Platform platform) {
        platforms.remove(platform);
        return getLatestVersion();
    }

    /**
     * Class for handling versions (only supports numbers [letters/words will be ignored]) to allow for easy comparison. For Minecraft versions use {@link SemanticVersion} instead!
     * <p><b>This will work best if the version is in <a href="https://semver.org">semantic format</a></b>
     */
    private static class Version {
        /**
         * The version as a {@link String}
         */
        @NotNull public final String string;
        /**
         * The value of the version
         */
        private int value = 0;

        /**
         * Creates a new {@link Version} object
         *
         * @param   string  {@link #string}
         */
        public Version(@NotNull String string) {
            this.string = string;

            // Set value
            final List<Integer> values = new ArrayList<>();
            for (final String subString : string.split("\\.")) {
                try {
                    values.add(Integer.parseInt(subString));
                } catch (final NumberFormatException e) {
                    break;
                }
            }

            final int length = values.size();
            for (int i = 0; i < length; i++) value += values.get(i) * Math.pow(10, length - i);
        }
    }
}
