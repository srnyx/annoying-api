package xyz.srnyx.annoyingapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;

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
import java.util.Optional;
import java.util.logging.Level;


/**
 * Class for handling update checking
 */
public class AnnoyingUpdate extends Stringable implements Annoyable {
    /**
     * The current version of Minecraft in short form (ex: 1.17 instead of 1.17.0)
     */
    @NotNull private static final String MINECRAFT_VERSION_SHORT = AnnoyingPlugin.MINECRAFT_VERSION.patch != 0 ? AnnoyingPlugin.MINECRAFT_VERSION.version : AnnoyingPlugin.MINECRAFT_VERSION.major + "." + AnnoyingPlugin.MINECRAFT_VERSION.minor;

    /**
     * The {@link AnnoyingPlugin plugin} instance
     */
    @NotNull private final AnnoyingPlugin annoyingPlugin;
    /**
     * The name of the plugin to check for updates
     */
    @NotNull private final String pluginName;
    /**
     * The current version of the plugin
     */
    @NotNull private final SemanticVersion currentVersion;
    /**
     * The user agent to use when making requests
     */
    @NotNull private final String userAgent;
    /**
     * The platforms the plugin is available on. Currently only {@link PluginPlatform.Platform#MODRINTH Modrinth}, {@link PluginPlatform.Platform#HANGAR Hangar}, and {@link PluginPlatform.Platform#SPIGOT Spigot} are supported
     */
    @NotNull private final PluginPlatform.Multi platforms;
    /**
     * The latest version of the plugin
     */
    @Nullable public final SemanticVersion latestVersion;

    /**
     * Creates a new {@link AnnoyingUpdate} object
     *
     * @param   annoyingPlugin      {@link #annoyingPlugin}
     * @param   pluginDescription   {@link #pluginName} and {@link #currentVersion}
     * @param   platforms           {@link #platforms}
     */
    public AnnoyingUpdate(@NotNull AnnoyingPlugin annoyingPlugin, @NotNull PluginDescriptionFile pluginDescription, @NotNull PluginPlatform.Multi platforms) {
        this.annoyingPlugin = annoyingPlugin;
        this.pluginName = pluginDescription.getName();
        this.currentVersion = new SemanticVersion(pluginDescription.getVersion());
        this.userAgent = annoyingPlugin.getName() + "/" + annoyingPlugin.getDescription().getVersion() + " via Annoying API (update)";
        this.platforms = platforms;
        this.latestVersion = getLatestVersion()
                .map(SemanticVersion::new)
                .orElse(null);
    }

    /**
     * Creates a new {@link AnnoyingUpdate} object
     *
     * @param   annoyingPlugin  {@link #annoyingPlugin}
     * @param   plugin          {@link #pluginName} and {@link #currentVersion}
     * @param   platforms       {@link #platforms}
     */
    public AnnoyingUpdate(@NotNull AnnoyingPlugin annoyingPlugin, @NotNull PluginBase plugin, @NotNull PluginPlatform.Multi platforms) {
        this(annoyingPlugin, plugin.getDescription(), platforms);
    }

    /**
     * Creates a new {@link AnnoyingUpdate} object
     *
     * @param   plugin      {@link #annoyingPlugin}, {@link #pluginName}, and {@link #currentVersion}
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
    @SuppressWarnings("UnusedReturnValue")
    public boolean checkUpdate() {
        final boolean update = isUpdateAvailable();
        if (update && latestVersion != null) new AnnoyingMessage(annoyingPlugin, annoyingPlugin.options.messagesOptions.keys.updateAvailable)
                .replace("%plugin%", pluginName)
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

    @NotNull
    private Optional<String> getLatestVersion() {
        // Modrinth
        final Optional<String> modrinthIdentifier = platforms.getIdentifier(PluginPlatform.Platform.MODRINTH);
        if (modrinthIdentifier.isPresent()) {
            try {
                final Optional<String> modrinth = modrinth(modrinthIdentifier.get());
                if (modrinth.isPresent()) return modrinth;
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "Failed to check Modrinth for the latest version of " + pluginName, e);
                return fail(PluginPlatform.Platform.MODRINTH);
            }
        }

        // Hangar
        final Optional<PluginPlatform> hangarPlatform = platforms.get(PluginPlatform.Platform.HANGAR);
        if (hangarPlatform.isPresent()) {
            try {
                final Optional<String> hangar = hangar(hangarPlatform.get());
                if (hangar.isPresent()) return hangar;
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "Failed to check Hangar for the latest version of " + pluginName, e);
                return fail(PluginPlatform.Platform.HANGAR);
            }
        }

        // Spigot
        final Optional<String> spigotIdentifier = platforms.getIdentifier(PluginPlatform.Platform.SPIGOT);
        if (spigotIdentifier.isPresent()) {
            try {
                final Optional<String> spigot = spigot(spigotIdentifier.get());
                if (spigot.isPresent()) return spigot;
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "Failed to check Spigot for the latest version of " + pluginName, e);
                return fail(PluginPlatform.Platform.SPIGOT);
            }
        }

        // No platforms left
        return Optional.empty();
    }

    /**
     * Checks Modrinth for the latest version
     *
     * @param   identifier  the identifier of the plugin on Modrinth
     *
     * @return              the latest version, or empty if an error occurred
     */
    @NotNull
    private Optional<String> modrinth(@NotNull String identifier) {
        try {
            final Optional<JsonArray> json = HttpUtility.getJson(userAgent,
                    "https://api.modrinth.com/v2/project/" + identifier + "/version" +
                            "?loaders=%5B%22spigot%22,%22paper%22,%22purpur%22%5D" +
                            "&game_versions=%5B%22" + MINECRAFT_VERSION_SHORT + "%22%5D")
                    .map(JsonElement::getAsJsonArray);

            // Request failed
            if (!json.isPresent()) return fail(PluginPlatform.Platform.MODRINTH);

            // Return the latest version
            return json.get().size() != 0 ? json.map(versions -> versions.get(0).getAsJsonObject().get("version_number").getAsString()) : fail(PluginPlatform.Platform.MODRINTH);
        } catch (final IllegalStateException e) {
            return fail(PluginPlatform.Platform.MODRINTH);
        }
    }

    /**
     * Checks Hangar for the latest version
     *
     * @param   platform    the {@link PluginPlatform} information
     *
     * @return              the latest version, or empty if an error occurred
     */
    @NotNull
    private Optional<String> hangar(@NotNull PluginPlatform platform) {
        final Optional<JsonArray> json = HttpUtility.getJson(userAgent, "https://hangar.papermc.io/api/v1/projects/" + platform.author + "/" + platform.identifier + "/versions")
                .map(element -> element.getAsJsonObject().getAsJsonArray("result"));

        // Request failed
        if (!json.isPresent()) return fail(PluginPlatform.Platform.HANGAR);

        // Get supported versions
        final Map<String, OffsetDateTime> result = new HashMap<>();
        try {
            for (final JsonElement versionElement : json.get()) {
                final JsonObject version = versionElement.getAsJsonObject();
                final JsonObject platformsObject = version.getAsJsonObject("platformDependencies");
                if (platformsObject == null) continue;
                final JsonArray paper = platformsObject.getAsJsonArray("PAPER");
                if (paper != null) for (final JsonElement paperElement : paper) {
                    final String paperVersion = paperElement.getAsString();
                    if (paperVersion.equals(MINECRAFT_VERSION_SHORT)) {
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
        final Optional<String> latest = result.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
        return latest.isPresent() ? latest : fail(PluginPlatform.Platform.HANGAR);
    }

    /**
     * Checks Spigot for the latest version
     *
     * @param   identifier  the identifier of the plugin on Spigot
     *
     * @return              the latest version, or empty if an error occurred
     */
    @NotNull
    private Optional<String> spigot(@NotNull String identifier) {
        final Optional<String> json = HttpUtility.getJson(userAgent, "https://api.spiget.org/v2/resources/" + identifier + "/versions/latest")
                .map(element -> element.getAsJsonObject().get("name").getAsString());

        // Request failed
        if (!json.isPresent()) return fail(PluginPlatform.Platform.SPIGOT);

        // Return the latest version
        return json;
    }

    /**
     * Remove the failed platform from the list of platforms and retry {@link #getLatestVersion() getting the latest version}
     *
     * @param   platform    the platform that failed
     *
     * @return              the latest version, or empty if an error occurred
     */
    @NotNull
    private Optional<String> fail(@NotNull PluginPlatform.Platform platform) {
        platforms.remove(platform);
        return getLatestVersion();
    }
}
