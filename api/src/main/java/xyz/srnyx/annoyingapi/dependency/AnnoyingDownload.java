package xyz.srnyx.annoyingapi.dependency;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.PluginPlatform;
import xyz.srnyx.annoyingapi.parents.Annoyable;
import xyz.srnyx.annoyingapi.parents.Stringable;
import xyz.srnyx.annoyingapi.utility.HttpConnectionUtility;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;


/**
 * Used for downloading {@link AnnoyingDependency}s
 */
public class AnnoyingDownload extends Stringable implements Annoyable {
    @NotNull private static final CommandRegister COMMAND_REGISTER = new CommandRegister();

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
    public AnnoyingDownload(@NotNull AnnoyingPlugin plugin, @NotNull List<AnnoyingDependency> dependencies) {
        this.plugin = plugin;
        this.userAgent = plugin.getName() + "/" + plugin.getDescription().getVersion() + " via AnnoyingAPI (dependency)";
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

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
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
     * Retry downloading the dependency with a new {@link PluginPlatform}
     */
    private void attemptDownload(@NotNull AnnoyingDependency dependency) {
        final String name = dependency.name;
        final PluginPlatform.Multi platforms = dependency.platforms;
        
        // Modrinth
        final String modrinth = platforms.getIdentifier(PluginPlatform.Platform.MODRINTH);
        if (modrinth != null) {
            modrinth(dependency, modrinth);
            return;
        }

        // Hangar
        final PluginPlatform hangar = platforms.get(PluginPlatform.Platform.HANGAR);
        if (hangar != null) {
            hangar(dependency, hangar);
            return;
        }

        // Spigot
        final String spigot = platforms.getIdentifier(PluginPlatform.Platform.SPIGOT);
        if (spigot != null) {
            spigot(dependency, spigot);
            return;
        }

        // Bukkit
        final String bukkit = platforms.getIdentifier(PluginPlatform.Platform.BUKKIT);
        if (bukkit != null) {
            downloadFile(dependency, PluginPlatform.Platform.BUKKIT, "https://dev.bukkit.org/projects/" + bukkit + "/files/latest");
            return;
        }

        // External
        final String external = platforms.getIdentifier(PluginPlatform.Platform.EXTERNAL);
        if (external != null) {
            downloadFile(dependency, PluginPlatform.Platform.EXTERNAL, external);
            return;
        }

        // Manual
        final String manual = platforms.getIdentifier(PluginPlatform.Platform.MANUAL);
        if (manual != null) {
            AnnoyingPlugin.log(Level.WARNING, "&6" + name + " &8|&e Please install this plugin manually at &6" + manual);
            finish(dependency, false);
            return;
        }

        // Ran out of platforms
        AnnoyingPlugin.log(Level.SEVERE, "&4" + name + " &8|&c Ran out of platforms!");
        finish(dependency, false);
    }

    /**
     * Downloads the plugin from Modrinth using their Labrinth API
     * <p>This will download the appropriate Minecraft version of the plugin
     *
     * @param   dependency  the {@link AnnoyingDependency} to download
     * @param   identifier  the identifier of the plugin on Modrinth
     */
    private void modrinth(@NotNull AnnoyingDependency dependency, @NotNull String identifier) {
        final JsonElement json = HttpConnectionUtility.requestJson(userAgent,
                "https://api.modrinth.com/v2/project/" + identifier + "/version" +
                        "?loaders=%5B%22spigot%22,%22paper%22,%22purpur%22%5D" +
                        "&game_versions=%5B%22" + AnnoyingPlugin.MINECRAFT_VERSION.version + "%22%5D");

        // Request failed
        if (json == null) {
            fail(dependency, PluginPlatform.Platform.MODRINTH);
            return;
        }

        // Download file
        downloadFile(dependency, PluginPlatform.Platform.MODRINTH, json.getAsJsonArray().get(0).getAsJsonObject()
                .getAsJsonArray("files").get(0).getAsJsonObject()
                .get("url").getAsString());
    }

    /**
     * Downloads the plugin from Spigot using their API
     * <p>This will download the appropriate Minecraft version of the plugin
     *
     * @param   dependency  the {@link AnnoyingDependency} to download
     * @param   platform    the {@link PluginPlatform} containing the plugin information
     */
    private void hangar(@NotNull AnnoyingDependency dependency, @NotNull PluginPlatform platform) {
        if (platform.author == null) {
            fail(dependency, platform.platform);
            return;
        }
        final String url = "https://hangar.papermc.io/api/v1/projects/" + platform.author + "/" + platform.identifier + "/";

        final String latest = HttpConnectionUtility.requestString(userAgent, url + "latestrelease");
        // Request failed
        if (latest == null) {
            fail(dependency, platform.platform);
            return;
        }

        // Download file
        downloadFile(dependency, platform.platform, url + "versions/" + latest + "/PAPER/download");
    }

    /**
     * Downloads the plugin from Spigot using Spiget API
     * <p>This will check if the plugin is premium and/or external before attempting to download
     *
     * @param   dependency  the {@link AnnoyingDependency} to download
     * @param   identifier  the identifier of the plugin on Spigot
     */
    private void spigot(@NotNull AnnoyingDependency dependency, @NotNull String identifier) {
        final PluginPlatform.Multi platforms = dependency.platforms;
        final String url = "https://api.spiget.org/v2/resources/" + identifier;
        final JsonElement json = HttpConnectionUtility.requestJson(userAgent, url);

        // Request failed
        if (json == null) {
            fail(dependency, PluginPlatform.Platform.SPIGOT);
            return;
        }
        final JsonObject object = json.getAsJsonObject();

        // Resource is premium
        if (object.get("premium").getAsBoolean()) {
            fail(dependency, PluginPlatform.Platform.SPIGOT);
            return;
        }

        // Resource is external
        if (object.get("external").getAsBoolean()) {
            platforms.remove(PluginPlatform.Platform.SPIGOT);

            // Get external URL and set new platform
            final String externalUrl = object
                    .get("file").getAsJsonObject()
                    .get("externalUrl").getAsString();
            if (externalUrl.endsWith(".jar")) {
                platforms.addIfAbsent(PluginPlatform.external(externalUrl));
            } else {
                platforms.addIfAbsent(PluginPlatform.manual(externalUrl));
            }

            // Retry download
            attemptDownload(dependency);
            return;
        }

        // Download file
        downloadFile(dependency, PluginPlatform.Platform.SPIGOT,  url + "/download");
    }

    /**
     * Downloads a file from a URL
     *
     * @param   dependency  the {@link AnnoyingDependency} to download
     * @param   platform    the platform of the URL
     * @param   urlString   the URL of the file
     */
    private void downloadFile(@NotNull AnnoyingDependency dependency, @NotNull PluginPlatform.Platform platform, @NotNull String urlString) {
        // Get URL connection
        final HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
        } catch (final IOException e) {
            e.printStackTrace();
            fail(dependency, platform);
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
        AnnoyingPlugin.log(Level.INFO, "&2" + dependency.name + " &8|&a Successfully downloaded from &2" + platform.name());
        finish(dependency, true);
    }

    /**
     * Removes the specified platform from the dependency and attempts to download the dependency again
     *
     * @param   dependency  the {@link AnnoyingDependency} being processed
     * @param   platform    the platform to remove
     */
    private void fail(@NotNull AnnoyingDependency dependency, @NotNull PluginPlatform.Platform platform) {
        dependency.platforms.remove(platform);
        attemptDownload(dependency);
    }

    /**
     * Finishes the download and checks if all plugins have been processed
     *
     * @param   dependency  the dependency that was just processed
     * @param   enable      whether to enable the dependency
     */
    private void finish(@NotNull AnnoyingDependency dependency, boolean enable) {
        // Load/enable plugin and register its commands
        final PluginManager manager = Bukkit.getPluginManager();
        if (enable && dependency.enableAfterDownload && dependency.isNotInstalled()) {
            try {
                // Load and enable plugin
                final Plugin dependencyPlugin = manager.loadPlugin(dependency.file);
                dependencyPlugin.onLoad();
                manager.enablePlugin(dependencyPlugin);
                // Register commands
                PluginCommandYamlParser.parse(dependencyPlugin).forEach(command -> COMMAND_REGISTER.register(dependencyPlugin, command));
                COMMAND_REGISTER.sync();
            } catch (final IllegalArgumentException | InvalidPluginException | InvalidDescriptionException e) {
                AnnoyingPlugin.log(Level.SEVERE, "&4" + dependency.name + " &8|&c Failed to load plugin!");
            }
        }

        // Check if all plugins have been processed
        remaining--;
        if (remaining == 0) {
            AnnoyingPlugin.log(Level.INFO, "&a&lAll &2&l" + dependencies.size() + "&a&l plugins have been processed! &aPlease resolve any errors and then restart the server.");
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                if (finishRunnable != null) finishRunnable.run();
                return null;
            });
        }
    }
}
