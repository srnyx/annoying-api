package xyz.srnyx.annoyingapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.message.AnnoyingMessages;
import xyz.srnyx.annoyingapi.parents.AnnoyableClass;
import xyz.srnyx.javautilities.HttpUtility;
import xyz.srnyx.javautilities.MiscUtility;
import xyz.srnyx.javautilities.manipulation.Mapper;
import xyz.srnyx.javautilities.objects.SemanticVersion;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;


/**
 * Class for handling update checking
 */
public class UpdateChecker extends AnnoyableClass {
    /**
     * The current version of Minecraft in short form (ex: 1.17 instead of 1.17.0)
     */
    @NotNull private static final String MINECRAFT_VERSION_SHORT = AnnoyingPlugin.MINECRAFT_VERSION.patch != 0 ? AnnoyingPlugin.MINECRAFT_VERSION.version : AnnoyingPlugin.MINECRAFT_VERSION.major + "." + AnnoyingPlugin.MINECRAFT_VERSION.minor;
    @NotNull private static final String SPIGET_RESOURCES_URL = "https://api.spiget.org/v2/resources/";

    /**
     * The name of the plugin to check for updates
     */
    @NotNull private final String pluginName;
    /**
     * The current version of the plugin
     */
    @NotNull private final String currentVersion;
    /**
     * {@code null} if on a snapshot/development version
     */
    @Nullable private final SemanticVersion currentVersionSemantic;
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
    @Nullable public final LatestVersion latestVersion;

    /**
     * Creates a new {@link UpdateChecker} object
     *
     * @param   annoyingPlugin      {@link #annoyingPlugin}
     * @param   pluginDescription   {@link #pluginName} and {@link #currentVersion}
     * @param   platforms           {@link #platforms}
     */
    public UpdateChecker(@NotNull AnnoyingPlugin annoyingPlugin, @NotNull PluginDescriptionFile pluginDescription, @NotNull PluginPlatform.Multi platforms) {
        super(annoyingPlugin);
        this.pluginName = pluginDescription.getName();
        this.currentVersion = pluginDescription.getVersion();
        this.currentVersionSemantic = MiscUtility.handleException(() -> new SemanticVersion(currentVersion)).orElse(null);
        this.userAgent = annoyingPlugin.getName() + "/" + annoyingPlugin.getDescription().getVersion() + " via Annoying API (update)";
        this.platforms = new PluginPlatform.Multi(platforms);
        this.latestVersion = retrieveLatestVersion();
    }

    /**
     * Creates a new {@link UpdateChecker} object
     *
     * @param   annoyingPlugin  {@link #annoyingPlugin}
     * @param   plugin          {@link #pluginName} and {@link #currentVersion}
     * @param   platforms       {@link #platforms}
     */
    public UpdateChecker(@NotNull AnnoyingPlugin annoyingPlugin, @NotNull PluginBase plugin, @NotNull PluginPlatform.Multi platforms) {
        this(annoyingPlugin, plugin.getDescription(), platforms);
    }

    /**
     * Creates a new {@link UpdateChecker} object
     *
     * @param   plugin      {@link #annoyingPlugin}, {@link #pluginName}, and {@link #currentVersion}
     * @param   platforms   {@link #platforms}
     */
    public UpdateChecker(@NotNull AnnoyingPlugin plugin, @NotNull PluginPlatform.Multi platforms) {
        this(plugin, plugin, platforms);
    }

    @Override @NotNull
    public String toString() {
        return Stringable.toString(this);
    }

    /**
     * Checks if an update is available and sends {@link AnnoyingMessages.Plugin#update_available a message} to the console if it is
     *
     * @return  {@code true} if an update is available, {@code false} otherwise
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean checkUpdate() {
        final boolean update = isUpdateAvailable();
        if (update) annoyingPlugin.getAnnoyingMessages().plugin.update_available.newMessage()
                .replace("%plugin%", pluginName)
                .replace("%current%", currentVersion)
                .replace("%new%", Objects.requireNonNull(latestVersion).version.version)
                .replace("%link%", latestVersion.link)
                .log(Level.WARNING);
        return update;
    }

    /**
     * Checks if an update is available
     *
     * @return  {@code true} if an update is available, {@code false} otherwise
     */
    public boolean isUpdateAvailable() {
        if (latestVersion == null) return false;
        if (currentVersionSemantic == null) return true;
        return latestVersion.version.isGreaterThan(currentVersionSemantic);
    }

    @Nullable
    private LatestVersion retrieveLatestVersion() {
        for (final PluginPlatform platform : platforms) {
            try {
                final Optional<LatestVersion> version = switch (platform.platform) {
                    case MODRINTH -> modrinth(platform.identifier);
                    case HANGAR -> hangar(platform.identifier);
                    case SPIGOT -> spigot(platform.identifier);
                    default -> Optional.empty();
                };
                if (version.isPresent()) return version.get();
            } catch (final Exception e) {
                annoyingPlugin.errorTrack("Failed to check " + platform.platform + " for the latest version of " + pluginName, e);
            }
        }

        AnnoyingPlugin.log(Level.WARNING, "&cRan out of platforms to check for the latest version of &4" + pluginName + "&c (platforms: " + platforms + ")");
        return null;
    }

    /**
     * Checks Modrinth for the latest version
     *
     * @param   identifier  the identifier of the plugin on Modrinth
     *
     * @return              the latest version, or empty if an error occurred
     */
    @NotNull
    private Optional<LatestVersion> modrinth(@NotNull String identifier) {
        return HttpUtility.getJson(userAgent,
                "https://api.modrinth.com/v2/project/" + identifier + "/version" +
                        "?loaders=%5B%22spigot%22,%22paper%22,%22purpur%22%5D" +
                        "&game_versions=%5B%22" + MINECRAFT_VERSION_SHORT + "%22%5D" +
                        "&include_changelog=false", null)
                .flatMap(element -> Mapper.convertJsonElement(element, JsonArray.class))
                .filter(versions -> versions.size() != 0)
                .map(versions -> {
                    for (final JsonElement version : versions) {
                        final JsonObject versionObject = Mapper.convertJsonElement(version, JsonObject.class).orElse(null);
                        if (versionObject == null) continue;
                        final String versionType = Mapper.convertJsonElementToPrimitive(versionObject.get("version_type"), String.class).orElse(null);
                        if (versionType != null && versionType.equals("release")) return versionObject;
                    }
                    return null;
                })
                .map(version -> {
                    final String id = Mapper.convertJsonElementToPrimitive(version.get("id"), String.class).orElse(null);
                    if (id == null) return null;
                    final String versionNumber = Mapper.convertJsonElementToPrimitive(version.get("version_number"), String.class).orElse(null);
                    if (versionNumber == null) return null;
                    return new LatestVersion(versionNumber, "https://modrinth.com/project/" + identifier + "/version/" + id);
                });
    }

    /**
     * Checks Hangar for the latest version
     *
     * @param   identifier  the identifier of the plugin on Hangar (author/name)
     *
     * @return              the latest version, or empty if an error occurred
     */
    @NotNull
    private Optional<LatestVersion> hangar(@NotNull String identifier) {
        return HttpUtility.getJson(userAgent,
                        "https://hangar.papermc.io/api/v1/projects/" + identifier + "/versions" +
                        "?platform=PAPER" +
                        "&platformVersion=" + MINECRAFT_VERSION_SHORT +
                        "&channel=Release&limit=1",null)
                .flatMap(element -> Mapper.convertJsonElement(element, JsonObject.class))
                .flatMap(object -> Mapper.convertJsonElement(object.get("result"), JsonArray.class))
                .filter(array -> array.size() != 0)
                .flatMap(version -> Mapper.convertJsonElement(version.get(0), JsonObject.class))
                .flatMap(object -> Mapper.convertJsonElementToPrimitive(object.get("name"), String.class))
                .map(version -> new LatestVersion(version, "https://hangar.papermc.io/" + identifier + "/versions/" + version));
    }

    /**
     * Checks Spigot for the latest version
     *
     * @param   identifier  the identifier of the plugin on Spigot
     *
     * @return              the latest version, or empty if an error occurred
     */
    @NotNull
    private Optional<LatestVersion> spigot(@NotNull String identifier) {
        return HttpUtility.getJson(userAgent, SPIGET_RESOURCES_URL + identifier + "/versions/latest", null)
                .flatMap(element -> Mapper.convertJsonElement(element, JsonObject.class))
                .flatMap(object -> Mapper.convertJsonElementToPrimitive(object.get("name"), String.class))
                .map(version -> {
                    // spigotmc.org/resources/IDENTIFIER/update?update=UPDATE else spigotmc.org/resources/IDENTIFIER/updates
                    final String update = "https://spigotmc.org/resources/" + identifier + "/update" + HttpUtility.getJson(userAgent, SPIGET_RESOURCES_URL + identifier + "/updates/latest", null)
                            .flatMap(element -> Mapper.convertJsonElement(element, JsonObject.class))
                            .flatMap(updateObject -> Mapper.convertJsonElementToPrimitive(updateObject.get("id"), Long.class))
                            .map(updateId -> "?update=" + updateId)
                            .orElse("s");
                    return new LatestVersion(version, update);
                });
    }

    public record LatestVersion(@NotNull SemanticVersion version, @NotNull String link) {
        public LatestVersion(@NotNull String version, @NotNull String link) {
            this(new SemanticVersion(version), link);
        }
    }
}
