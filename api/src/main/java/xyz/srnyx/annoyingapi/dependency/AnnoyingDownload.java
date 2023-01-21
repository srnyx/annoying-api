package xyz.srnyx.annoyingapi.dependency;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


/**
 * Used for downloading {@link AnnoyingDependency}s
 */
public class AnnoyingDownload {
    @NotNull private final AnnoyingPlugin plugin;
    @NotNull private final String userAgent;
    @NotNull private final List<AnnoyingDependency> dependencies;
    @Nullable private Runnable finishRunnable;
    private int remaining = 0;

    /**
     * Constructor for {@link AnnoyingDownload} with multiple dependencies
     *
     * @param   plugin          the plugin that is downloading the dependencies
     * @param   dependencies    the {@link AnnoyingDependency}s to download
     */
    @Contract(pure = true)
    public AnnoyingDownload(@NotNull AnnoyingPlugin plugin, @NotNull List<AnnoyingDependency> dependencies) {
        this.plugin = plugin;
        this.userAgent = plugin.getName() + "/" + plugin.getDescription().getVersion() + " via AnnoyingAPI";
        this.dependencies = dependencies;
    }

    /**
     * Constructor for {@link AnnoyingDownload} with a single dependency
     *
     * @param   plugin      the plugin that is downloading the {@link AnnoyingDependency}
     * @param   dependency  the {@link AnnoyingDependency} to download
     */
    public AnnoyingDownload(@NotNull AnnoyingPlugin plugin, @NotNull AnnoyingDependency dependency) {
        this(plugin, Collections.singletonList(dependency));
    }

    /**
     * Downloads the plugins
     *
     * @param   finishRunnable  the {@link Runnable} to run when all plugins have been downloaded
     */
    public void downloadPlugins(@Nullable Runnable finishRunnable) {
        this.finishRunnable = finishRunnable;
        this.remaining = dependencies.size();
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        dependencies.forEach(dependency -> scheduler.runTaskAsynchronously(plugin, () -> attemptDownload(dependency)));
    }

    /**
     * Retry downloading the dependency with a new {@link Platform}
     */
    private void attemptDownload(@NotNull AnnoyingDependency dependency) {
        final String name = dependency.name;
        final Map<Platform, String> platforms = dependency.platforms;
        
        // Modrinth
        if (platforms.containsKey(Platform.MODRINTH)) {
            modrinth(dependency);
            return;
        }

        // Spigot
        if (platforms.containsKey(Platform.SPIGOT)) {
            spigot(dependency);
            return;
        }

        // Bukkit
        if (platforms.containsKey(Platform.BUKKIT)) {
            downloadFile(dependency, Platform.BUKKIT, "https://dev.bukkit.org/projects/" + platforms.get(Platform.BUKKIT) + "/files/latest");
            return;
        }

        // External
        if (platforms.containsKey(Platform.EXTERNAL)) {
            downloadFile(dependency, Platform.EXTERNAL, platforms.get(Platform.EXTERNAL));
            return;
        }

        // Manual
        if (platforms.containsKey(Platform.MANUAL)) {
            plugin.log(Level.WARNING, "&6" + name + " &8|&e Please install this plugin manually at &6" + platforms.get(Platform.MANUAL));
            finish(dependency, false);
            return;
        }

        // Ran out of platforms
        plugin.log(Level.SEVERE, "&4" + name + " &8|&c Ran out of platforms!");
        finish(dependency, false);
    }

    /**
     * Downloads the plugin from Modrinth using their Labrinth API
     * <p>This will download the appropriate Minecraft version of the plugin
     */
    private void modrinth(@NotNull AnnoyingDependency dependency) {
        final String[] version = Bukkit.getBukkitVersion().split("\\.");
        final String url = "https://api.modrinth.com/v2/project/" + dependency.platforms.get(Platform.MODRINTH) + "/version" +
                "?loaders=%5B%22spigot%22,%22paper%22,%22purpur%22%5D" +
                "&game_versions=%5B%22" + version[0] + "." + version[1] + "." + version[2].split("-")[0] + "%22%5D";
        final JsonElement json = getJson(url);

        // Request failed
        if (json == null) {
            dependency.platforms.remove(Platform.MODRINTH);
            attemptDownload(dependency);
            return;
        }

        // Download file
        downloadFile(dependency, Platform.MODRINTH, json.getAsJsonArray().get(0).getAsJsonObject()
                .getAsJsonArray("files").get(0).getAsJsonObject()
                .get("url").getAsString());
    }

    /**
     * Downloads the plugin from Spigot using Spiget API
     * <p>This will check if the plugin is premium and/or external before attempting to download
     */
    private void spigot(@NotNull AnnoyingDependency dependency) {
        final Map<Platform, String> platforms = dependency.platforms;
        final String url = "https://api.spiget.org/v2/resources/" + platforms.get(Platform.SPIGOT);
        final JsonElement json = getJson(url);

        // Request failed
        if (json == null) {
            platforms.remove(Platform.SPIGOT);
            attemptDownload(dependency);
            return;
        }
        final JsonObject object = json.getAsJsonObject();

        // Resource is premium
        if (object.get("premium").getAsBoolean()) {
            platforms.remove(Platform.SPIGOT);
            attemptDownload(dependency);
            return;
        }

        // Resource is external
        if (object.get("external").getAsBoolean()) {
            platforms.remove(Platform.SPIGOT);

            // Get external URL and set new platform
            final String externalUrl = object
                    .get("file").getAsJsonObject()
                    .get("externalUrl").getAsString();
            if (externalUrl.endsWith(".jar")) {
                platforms.putIfAbsent(Platform.EXTERNAL, externalUrl);
            } else {
                platforms.putIfAbsent(Platform.MANUAL, externalUrl);
            }

            // Retry download
            attemptDownload(dependency);
            return;
        }

        // Download file
        downloadFile(dependency, Platform.SPIGOT,  url + "/download");
    }

    /**
     * Retrieves the {@link JsonElement} from the specified URL
     *
     * @param   urlString   the URL to retrieve the {@link JsonElement} from
     *
     * @return              the {@link JsonElement} retrieved from the specified URL
     */
    @Nullable
    private JsonElement getJson(@NotNull String urlString) {
        final HttpURLConnection connection;
        final JsonElement json;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
            if (connection.getResponseCode() == 404) return null;
            json = new JsonParser().parse(new InputStreamReader(connection.getInputStream()));
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
        connection.disconnect();
        return json;
    }

    /**
     * Downloads a file from a URL
     *
     * @param   platform    the platform of the URL
     * @param   urlString   the URL of the file
     */
    private void downloadFile(@NotNull AnnoyingDependency dependency, @NotNull Platform platform, @NotNull String urlString) {
        // Get URL connection
        final HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
        } catch (final IOException e) {
            e.printStackTrace();
            dependency.platforms.remove(platform);
            attemptDownload(dependency);
            return;
        }

        // Download file
        try (final BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             final FileOutputStream out = new FileOutputStream(dependency.file)) {
            final byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) out.write(buffer, 0, numRead);
        } catch (final IOException ignored) {
            //ignored
        }

        // Send success message
        plugin.log(Level.INFO, "&2" + dependency.name + " &8|&a Successfully downloaded from &2" + platform.name());
        finish(dependency, true);
    }

    /**
     * Finishes the download and checks if all plugins have been processed
     *
     * @param   dependency  the dependency that was just processed
     * @param   enable      whether or not to enable the dependency
     */
    private void finish(@NotNull AnnoyingDependency dependency, boolean enable) {
        // Load/enable plugin and register its commands
        final PluginManager manager = Bukkit.getPluginManager();
        if (enable && dependency.enableAfterDownload && manager.getPlugin(dependency.name) == null) {
            try {
                // Load and enable plugin
                final Plugin dependencyPlugin = manager.loadPlugin(dependency.file);
                dependencyPlugin.onLoad();
                manager.enablePlugin(dependencyPlugin);

                // Register commands
                PluginCommandYamlParser.parse(dependencyPlugin).forEach(command -> plugin.commandRegister.register(dependencyPlugin, command));
                plugin.commandRegister.sync();
            } catch (final InvalidPluginException | InvalidDescriptionException e) {
                plugin.log(Level.SEVERE, "&4" + dependency.name + " &8|&c Failed to load plugin!");
            }
        }

        // Check if all plugins have been processed
        remaining--;
        if (remaining == 0) {
            plugin.log(Level.INFO, "&a&lAll &2&l" + dependencies.size() + "&a&l plugins have been processed! &aPlease resolve any errors and then restart the server.");
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                if (finishRunnable != null) finishRunnable.run();
                return null;
            });
        }
    }

    /**
     * Platforms that plugins can be downloaded from
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
}
