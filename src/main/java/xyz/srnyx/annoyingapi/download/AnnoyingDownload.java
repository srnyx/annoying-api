package xyz.srnyx.annoyingapi.download;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.Bukkit;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;


/**
 * Used for downloading {@link AnnoyingDependency}s
 */
public class AnnoyingDownload {
    @NotNull private final AnnoyingPlugin plugin;
    @NotNull private final Set<AnnoyingDependency> plugins;
    private AnnoyingDownloadFinish finish;
    private int remaining = 0;

    /**
     * Constructor for {@link AnnoyingDownload} with multiple plugins
     *
     * @param   plugins the plugins (represented as {@link AnnoyingDependency}) to download
     */
    @Contract(pure = true)
    public AnnoyingDownload(@NotNull AnnoyingPlugin plugin, @NotNull Set<AnnoyingDependency> plugins) {
        this.plugin = plugin;
        this.plugins = plugins;
    }

    /**
     * Constructor for {@link AnnoyingDownload} with a single plugin
     *
     * @param   plugin  the plugin (represented as {@link AnnoyingDependency}) to download
     */
    public AnnoyingDownload(@NotNull AnnoyingPlugin plugin, @NotNull AnnoyingDependency dependency) {
        this(plugin, Set.of(dependency));
    }

    /**
     * Downloads the plugins
     *
     * @param   finish  the callback to run when all plugins have been processed
     */
    public void downloadPlugins(@Nullable AnnoyingDownloadFinish finish) {
        this.finish = finish;
        remaining = plugins.size();
        plugins.forEach(dependency -> new Thread(() -> attemptDownload(dependency)).start());
    }

    /**
     * Retry downloading the dependency with a new {@link AnnoyingPlatform}
     */
    private void attemptDownload(@NotNull AnnoyingDependency dependency) {
        final String name = dependency.name();
        final Map<AnnoyingPlatform, String> platforms = dependency.platforms();
        
        // Modrinth
        if (platforms.containsKey(AnnoyingPlatform.MODRINTH)) {
            modrinth(dependency);
            return;
        }

        // Spigot
        if (platforms.containsKey(AnnoyingPlatform.SPIGOT)) {
            spigot(dependency);
            return;
        }

        // Bukkit
        if (platforms.containsKey(AnnoyingPlatform.BUKKIT)) {
            downloadFile(dependency, AnnoyingPlatform.BUKKIT, "https://dev.bukkit.org/projects/" + platforms.get(AnnoyingPlatform.BUKKIT) + "/files/latest");
            return;
        }

        // External
        if (platforms.containsKey(AnnoyingPlatform.EXTERNAL)) {
            downloadFile(dependency, AnnoyingPlatform.EXTERNAL, platforms.get(AnnoyingPlatform.EXTERNAL));
            return;
        }

        // Manual
        if (platforms.containsKey(AnnoyingPlatform.MANUAL)) {
            plugin.log(Level.WARNING, "&6" + name + " &8|&e Please install this plugin manually at &6" + platforms.get(AnnoyingPlatform.MANUAL));
            finish();
            return;
        }

        // Ran out of platforms
        plugin.log(Level.SEVERE, "&4" + name + " &8|&c Ran out of platforms!");
        finish();
    }

    /**
     * Downloads the plugin from Modrinth using their Labrinth API
     * <p>This will download the appropriate Minecraft version of the plugin
     */
    private void modrinth(@NotNull AnnoyingDependency dependency) {
        final String[] version = Bukkit.getBukkitVersion().split("\\.");
        final String url = "https://api.modrinth.com/v2/project/" + dependency.platforms().get(AnnoyingPlatform.MODRINTH) + "/version" +
                "?loaders=%5B%22spigot%22,%22paper%22,%22purpur%22%5D" +
                "&game_versions=%5B%22" + version[0] + "." + version[1] + "." + version[2].split("-")[0] + "%22%5D";
        final JsonElement json = getJson(url);

        // Request failed
        if (json == null) {
            dependency.platforms().remove(AnnoyingPlatform.MODRINTH);
            attemptDownload(dependency);
            return;
        }

        // Download file
        downloadFile(dependency, AnnoyingPlatform.MODRINTH, json.getAsJsonArray().get(0).getAsJsonObject()
                .getAsJsonArray("files").get(0).getAsJsonObject()
                .get("url").getAsString());
    }

    /**
     * Downloads the plugin from Spigot using Spiget API
     * <p>This will check if the plugin is premium and/or external before attempting to download
     */
    private void spigot(@NotNull AnnoyingDependency dependency) {
        final Map<AnnoyingPlatform, String> platforms = dependency.platforms();
        final String url = "https://api.spiget.org/v2/resources/" + platforms.get(AnnoyingPlatform.SPIGOT);
        final JsonElement json = getJson(url);

        // Request failed
        if (json == null) {
            platforms.remove(AnnoyingPlatform.SPIGOT);
            attemptDownload(dependency);
            return;
        }
        final JsonObject object = json.getAsJsonObject();

        // Resource is premium
        if (object.get("premium").getAsBoolean()) {
            platforms.remove(AnnoyingPlatform.SPIGOT);
            attemptDownload(dependency);
            return;
        }

        // Resource is external
        if (object.get("external").getAsBoolean()) {
            platforms.put(AnnoyingPlatform.MANUAL, object
                    .get("file").getAsJsonObject()
                    .get("externalUrl").getAsString());
            platforms.remove(AnnoyingPlatform.SPIGOT);
            attemptDownload(dependency);
            return;
        }

        // Download file
        downloadFile(dependency, AnnoyingPlatform.SPIGOT,  url + "/download");
    }

    /**
     * Retrieves the {@link JsonElement} from the specified URL
     *
     * @param   url the URL to retrieve the {@link JsonElement} from
     *
     * @return      the {@link JsonElement} retrieved from the specified URL
     */
    @Nullable
    private JsonElement getJson(@NotNull String url) {
        try {
            final HttpResponse<String> response = HttpClient.newBuilder().build().send(HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) return null;
            return JsonParser.parseString(response.body());
        } catch (final IOException e) {
            return null;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Downloads a file from a URL
     *
     * @param   platform    the platform of the URL
     * @param   urlString   the URL of the file
     */
    private void downloadFile(@NotNull AnnoyingDependency dependency, @NotNull AnnoyingPlatform platform, @NotNull String urlString) {
        final String name = dependency.name();

        // Get URL
        final URL url;
        try {
            url = new URL(urlString);
        } catch (final MalformedURLException e) {
            dependency.platforms().remove(platform);
            attemptDownload(dependency);
            return;
        }

        // Download file
        try (final BufferedInputStream in = new BufferedInputStream(url.openStream());
             final FileOutputStream out = new FileOutputStream(new File(plugin.getDataFolder().getParentFile(), name + ".jar"))) {
            final byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) out.write(buffer, 0, numRead);
        } catch (final IOException ignored) {
            //ignored
        }

        // Send success message
        plugin.log(Level.INFO, "&2" + name + " &8|&a Successfully downloaded from &2" + platform.name());
        finish();
    }

    /**
     * Finishes the download and checks if all plugins have been processed
     */
    private void finish() {
        remaining--;
        if (remaining == 0) {
            plugin.log(Level.INFO, "\n&a&lAll &2&l" + plugins.size() + "&a&l plugins have been processed!\n&aPlease resolve any errors and then restart the server.");
            if (finish != null) finish.onFinish(plugins);
        }
    }
}
