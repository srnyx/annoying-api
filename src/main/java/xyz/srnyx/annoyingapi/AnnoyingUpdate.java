package xyz.srnyx.annoyingapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.message.AnnoyingMessage;
import xyz.srnyx.annoyingapi.options.MessagesOptions;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import xyz.srnyx.javautilities.HttpUtility;
import xyz.srnyx.javautilities.objects.SemanticVersion;
import xyz.srnyx.javautilities.parents.Stringable;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


/**
 * Class for handling update checking
 */
public class AnnoyingUpdate extends Stringable implements Annoyable {
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
    @NotNull private final SemanticVersion currentVersion;
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
    @Nullable public final SemanticVersion latestVersion;

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
        this.currentVersion = new SemanticVersion(plugin.getDescription().getVersion());
        this.userAgent = annoyingPlugin.getName() + "/" + annoyingPlugin.getDescription().getVersion() + " via Annoying API (update)";
        this.platforms = platforms;
        final String latestVersionString = getLatestVersion();
        this.latestVersion = latestVersionString == null ? null : new SemanticVersion(latestVersionString);
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

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return annoyingPlugin;
    }

    /**
     * Checks if an update is available and sends {@link MessagesOptions.MessageKeys#updateAvailable a message} to the console if it is
     *
     * @return  {@code true} if an update is available, {@code false} otherwise
     */
    public boolean checkUpdate() {
        final boolean update = isUpdateAvailable();
        if (update && latestVersion != null) new AnnoyingMessage(annoyingPlugin, annoyingPlugin.options.messagesOptions.keys.updateAvailable)
                .replace("%plugin%", plugin.getName())
                .replace("%current%", currentVersion.version)
                .replace("%new%", latestVersion.version)
                .log(Level.WARNING);
        return update;
    }

    /**
     * Checks if an update is available
     *
     * @return  {@code true} if an update is available, {@code false} otherwise
     */
    public boolean isUpdateAvailable() {
        return latestVersion != null && latestVersion.isGreaterThan(currentVersion);
    }

    @Nullable
    private String getLatestVersion() {
        // Modrinth
        final String modrinthIdentifier = platforms.getIdentifier(PluginPlatform.Platform.MODRINTH);
        if (modrinthIdentifier != null) {
            final String modrinth = modrinth(modrinthIdentifier);
            if (modrinth != null) return modrinth;
        }

        // Hangar
        final PluginPlatform hangarPlatform = platforms.get(PluginPlatform.Platform.HANGAR);
        if (hangarPlatform != null) {
            final String hangar = hangar(hangarPlatform);
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
    private String modrinth(@NotNull String identifier) {
        final JsonElement json = HttpUtility.getJson(userAgent,
                "https://api.modrinth.com/v2/project/" + identifier + "/version" +
                        "?loaders=%5B%22spigot%22,%22paper%22,%22purpur%22%5D" +
                        "&game_versions=%5B%22" + AnnoyingPlugin.MINECRAFT_VERSION.version + "%22%5D");

        // Request failed
        if (json == null) return fail(PluginPlatform.Platform.MODRINTH);

        // Return the latest version
        try {
            final JsonArray versions = json.getAsJsonArray();
            if (versions.size() == 0) return fail(PluginPlatform.Platform.MODRINTH);
            return versions.get(0).getAsJsonObject().get("version_number").getAsString();
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
    private String hangar(@NotNull PluginPlatform platform) {
        final JsonElement json = HttpUtility.getJson(userAgent, "https://hangar.papermc.io/api/v1/projects/" + platform.author + "/" + platform.identifier + "/versions");

        // Request failed
        if (json == null) return fail(PluginPlatform.Platform.HANGAR);

        // Get supported versions
        final Map<String, OffsetDateTime> result = new HashMap<>();
        final String minecraftVersion = AnnoyingPlugin.MINECRAFT_VERSION.version;
        try {
            for (final JsonElement versionElement : json.getAsJsonObject().get("versions").getAsJsonArray()) {
                final JsonObject version = versionElement.getAsJsonObject();
                final JsonObject platforms = version.getAsJsonObject("platformDependencies");
                if (platforms == null) continue;
                final JsonArray paper = platforms.getAsJsonArray("paper");
                if (paper != null) for (final JsonElement paperElement : paper) {
                    final String paperVersion = paperElement.getAsString();
                    if (paperVersion.equals(minecraftVersion)) {
                        final String name = version.get("name").getAsString();
                        final OffsetDateTime createdAt = OffsetDateTime.parse(version.get("createdAt").getAsString());

                        // If it's a duplicate version, keep the latest
                        final OffsetDateTime existing = result.get(name);
                        if (existing != null) {
                            if (createdAt.isAfter(existing)) result.put(name, createdAt);
                            break;
                        }

                        // Add the version to results
                        result.put(name, createdAt);
                        break;
                    }
                }
            }
        } catch (final Exception e) {
            return fail(PluginPlatform.Platform.HANGAR);
        }

        // Get the latest version
        return result.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(fail(PluginPlatform.Platform.HANGAR));
    }

    /**
     * Checks Spigot for the latest version
     *
     * @param   identifier  the identifier of the plugin on Spigot
     *
     * @return              the latest version, or {@code null} if an error occurred
     */
    @Nullable
    private String spigot(@NotNull String identifier) {
        final JsonElement json = HttpUtility.getJson(userAgent, "https://api.spiget.org/v2/resources/" + identifier + "/versions/latest");

        // Request failed
        if (json == null) return fail(PluginPlatform.Platform.SPIGOT);

        // Return the latest version
        return json.getAsJsonObject().get("name").getAsString();
    }

    /**
     * Remove the failed platform from the list of platforms and retry {@link #getLatestVersion() getting the latest version}
     *
     * @param   platform    the platform that failed
     *
     * @return              the latest version, or {@code null} if an error occurred
     */
    @Nullable
    private String fail(@NotNull PluginPlatform.Platform platform) {
        platforms.remove(platform);
        return getLatestVersion();
    }
}
