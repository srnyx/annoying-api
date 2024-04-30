package xyz.srnyx.annoyingapi;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ConfigurationUtility;

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * Contains information about a plugin on a {@link Platform}
 */
public class PluginPlatform extends Stringable {
    /**
     * The platform the plugin is on
     */
    @NotNull public final Platform platform;
    /**
     * The identifier of the plugin on the platform
     */
    @NotNull public final String identifier;
    /**
     * The author of the plugin (only required for {@link Platform#HANGAR})
     */
    @Nullable public String author;

    /**
     * Creates a new {@link PluginPlatform}
     *
     * @param   platform    {@link #platform}
     * @param   identifier  {@link #identifier}
     */
    public PluginPlatform(@NotNull Platform platform, @NotNull String identifier) {
        this.platform = platform;
        this.identifier = identifier;
        if (platform.requiresAuthor) AnnoyingPlugin.log(Level.WARNING, "&ePlugin platform &6" + platform + "&e requires an author");
    }

    /**
     * Creates a new {@link PluginPlatform}
     *
     * @param   platform    {@link #platform}
     * @param   identifier  {@link #identifier}
     * @param   author      {@link #author}
     */
    public PluginPlatform(@NotNull Platform platform, @NotNull String identifier, @Nullable String author) {
        this.platform = platform;
        this.identifier = identifier;
        this.author = author;
    }

    /**
     * Loads a {@link PluginPlatform} from a {@link ConfigurationSection}
     *
     * @param   section the section to load from
     *
     * @return          the loaded {@link PluginPlatform}
     */
    @Nullable
    public static PluginPlatform load(@NotNull ConfigurationSection section) {
        // platform
        final String name = section.getName();
        final String platformName = name.isEmpty() ? section.getString("platform") : name;
        if (platformName == null) {
            AnnoyingPlugin.log(Level.WARNING, "&6platform&e is null for section &6" + section.getCurrentPath());
            return null;
        }
        final Platform platform;
        try {
            platform = Platform.valueOf(platformName.toUpperCase());
        } catch (final IllegalArgumentException e) {
            AnnoyingPlugin.log(Level.WARNING, "&eInvalid platform &6" + platformName + "&e for section &6" + section.getCurrentPath());
            return null;
        }

        // identifier
        final String identifier = section.getString("identifier");
        if (identifier == null) {
            AnnoyingPlugin.log(Level.WARNING, "&eidentifier&e is null for platform &6" + platform.name());
            return null;
        }

        // author
        if (platform.requiresAuthor) {
            final String author = section.getString("author");
            if (author == null) {
                AnnoyingPlugin.log(Level.WARNING, "&eauthor&e is null for author-required platform &6" + platform.name() + "&e with identifier &6" + identifier);
                return null;
            }
            return new PluginPlatform(platform, identifier, author);
        }

        return new PluginPlatform(platform, identifier);
    }

    /**
     * Creates a new {@link PluginPlatform} for {@link Platform#MODRINTH}
     *
     * @param   identifier  {@link #identifier}
     *
     * @return              a new {@link PluginPlatform}
     */
    @NotNull
    public static PluginPlatform modrinth(@NotNull String identifier) {
        return new PluginPlatform(Platform.MODRINTH, identifier);
    }

    /**
     * Creates a new {@link PluginPlatform} for {@link Platform#HANGAR}
     *
     * @param   identifier  {@link #identifier}
     * @param   author      {@link #author}
     *
     * @return              a new {@link PluginPlatform}
     */
    @NotNull
    public static PluginPlatform hangar(@NotNull String identifier, @NotNull String author) {
        return new PluginPlatform(Platform.HANGAR, identifier, author);
    }

    /**
     * Creates a new {@link PluginPlatform} for {@link Platform#HANGAR}
     *
     * @param   identifier  {@link #identifier}
     * @param   plugin      the plugin to get the {@link #author} from
     *
     * @return              a new {@link PluginPlatform}
     */
    @Nullable
    public static PluginPlatform hangar(@NotNull String identifier, @NotNull Plugin plugin) {
        final List<String> authors = plugin.getDescription().getAuthors();
        if (authors.isEmpty()) {
            AnnoyingPlugin.log(Level.WARNING, "&eNo authors found for plugin &6" + plugin.getName() + "&e, but Hangar requires an author for identifier &6" + identifier);
            return null;
        }
        return hangar(identifier, authors.get(0));
    }

    /**
     * Creates a new {@link PluginPlatform} for {@link Platform#HANGAR}
     *
     * @param   plugin  the plugin to get the {@link #identifier} from
     * @param   author  {@link #author}
     *
     * @return          a new {@link PluginPlatform}
     */
    @NotNull
    public static PluginPlatform hangar(@NotNull Plugin plugin, @NotNull String author) {
        return hangar(plugin.getName(), author);
    }

    /**
     * Creates a new {@link PluginPlatform} for {@link Platform#HANGAR}
     *
     * @param   plugin  the plugin to get the {@link #identifier} and {@link #author} from
     *
     * @return          a new {@link PluginPlatform}
     */
    @Nullable
    public static PluginPlatform hangar(@NotNull Plugin plugin) {
        return hangar(plugin.getName(), plugin);
    }

    /**
     * Creates a new {@link PluginPlatform} for {@link Platform#SPIGOT}
     *
     * @param   identifier  {@link #identifier}
     *
     * @return              a new {@link PluginPlatform}
     */
    @NotNull
    public static PluginPlatform spigot(@NotNull String identifier) {
        return new PluginPlatform(Platform.SPIGOT, identifier);
    }

    /**
     * Creates a new {@link PluginPlatform} for {@link Platform#BUKKIT}
     *
     * @param   identifier  {@link #identifier}
     *
     * @return              a new {@link PluginPlatform}
     */
    @NotNull
    public static PluginPlatform bukkit(@NotNull String identifier) {
        return new PluginPlatform(Platform.BUKKIT, identifier);
    }

    /**
     * Creates a new {@link PluginPlatform} for {@link Platform#EXTERNAL}
     *
     * @param   identifier  {@link #identifier}
     *
     * @return              a new {@link PluginPlatform}
     */
    @NotNull
    public static PluginPlatform external(@NotNull String identifier) {
        return new PluginPlatform(Platform.EXTERNAL, identifier);
    }

    /**
     * Creates a new {@link PluginPlatform} for {@link Platform#MANUAL}
     *
     * @param   identifier  {@link #identifier}
     *
     * @return              a new {@link PluginPlatform}
     */
    @NotNull
    public static PluginPlatform manual(@NotNull String identifier) {
        return new PluginPlatform(Platform.MANUAL, identifier);
    }

    /**
     * Platforms that plugins can be downloaded (or checked for updates) from
     */
    public enum Platform {
        /**
         * <a href="https://modrinth.com/plugins">{@code https://modrinth.com/plugins}</a>
         * <p>Project ID <i>or</i> slug
         * <p><b>Example:</b> {@code gzktm9GG} <i>or</i> {@code annoying-api}
         */
        MODRINTH,
        /**
         * <a href="https://hangar.papermc.io/">{@code https://hangar.papermc.io/}</a>
         * <p>Slug
         * <p><b>Example:</b> {@code AnnoyingAPI}
         */
        HANGAR(true),
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
        MANUAL;

        /**
         * Whether this {@link Platform} requires an author
         */
        public final boolean requiresAuthor;

        Platform() {
            requiresAuthor = false;
        }

        Platform(boolean requiresAuthor) {
            this.requiresAuthor = requiresAuthor;
        }
    }

    /**
     * A collection of {@link PluginPlatform}s
     */
    public static class Multi {
        /**
         * The {@link PluginPlatform PluginPlatforms} in this {@link Multi Multi}
         */
        @NotNull public final Set<PluginPlatform> pluginPlatforms = new HashSet<>();

        /**
         * Creates a new empty {@link Multi}
         */
        public Multi() {
            // Creates a new empty Multi
        }

        /**
         * Creates a new {@link Multi} with the given {@link PluginPlatform}s
         *
         * @param   pluginPlatforms {@link #pluginPlatforms}
         */
        public Multi(@NotNull Collection<PluginPlatform> pluginPlatforms) {
            pluginPlatforms.forEach(this::addIfAbsent);
        }

        /**
         * Creates a new {@link Multi} with the given {@link PluginPlatform}s
         *
         * @param   pluginPlatforms {@link #pluginPlatforms}
         */
        public Multi(@NotNull PluginPlatform... pluginPlatforms) {
            this(Arrays.asList(pluginPlatforms));
        }

        /**
         * Loads a {@link Multi} from the given {@link ConfigurationSection}
         *
         * @param   section the {@link ConfigurationSection} to load from
         * @param   key     the key to load from
         *
         * @return          the loaded {@link Multi}
         */
        @NotNull
        public static Multi load(@NotNull ConfigurationSection section, @NotNull String key) {
            final Multi multi = new Multi();

            // Map list
            final ConfigurationSection platformsSection = section.getConfigurationSection(key);
            if (platformsSection == null) {
                ConfigurationUtility.toConfigurationList(section.getMapList(key)).stream()
                        .map(PluginPlatform::load)
                        .forEach(multi::addIfAbsent);
                return multi;
            }

            // Key list
            for (final String platformKey : platformsSection.getKeys(false)) {
                // String
                final ConfigurationSection platformSection = platformsSection.getConfigurationSection(platformKey);
                if (platformSection == null) {
                    final String platformKeyString = platformsSection.getString(platformKey);
                    if (platformKeyString != null) try {
                        multi.addIfAbsent(new PluginPlatform(Platform.valueOf(platformKey.toUpperCase()), platformKeyString));
                    } catch (final IllegalArgumentException ignored) {}
                    continue;
                }
                // Section
                final PluginPlatform pluginPlatform = PluginPlatform.load(platformSection);
                if (pluginPlatform != null) multi.addIfAbsent(pluginPlatform);
            }

            return multi;
        }

        /**
         * Gets the {@link PluginPlatform} for the given {@link Platform}
         *
         * @param   platform    the {@link Platform} to get the {@link PluginPlatform} for
         *
         * @return              the {@link PluginPlatform} for the given {@link Platform}
         */
        @Nullable
        public PluginPlatform get(@NotNull Platform platform) {
            return pluginPlatforms.stream()
                    .filter(filter -> filter.platform == platform)
                    .findFirst()
                    .orElse(null);
        }

        /**
         * Gets the {@link PluginPlatform#identifier} for the given {@link Platform}
         *
         * @param   platform    the {@link Platform} to get the {@link PluginPlatform#identifier} for
         *
         * @return              the {@link PluginPlatform#identifier} for the given {@link Platform}
         */
        @Nullable
        public String getIdentifier(@NotNull Platform platform) {
            final PluginPlatform pluginPlatform = get(platform);
            return pluginPlatform == null ? null : pluginPlatform.identifier;
        }

        /**
         * Adds the given {@link PluginPlatform plugin platforms} to this {@link Multi}
         *
         * @param   pluginPlatforms the {@link PluginPlatform plugin platforms} to add
         *
         * @return                  whether a {@link PluginPlatform plugin platform} was added
         */
        public boolean add(@NotNull PluginPlatform... pluginPlatforms) {
            return this.pluginPlatforms.addAll(Arrays.asList(pluginPlatforms));
        }

        /**
         * Adds the given {@link PluginPlatform plugin platforms} to this {@link Multi} if they don't already exist
         *
         * @param   pluginPlatforms the {@link PluginPlatform plugin platforms} to add
         *
         * @return                  whether a {@link PluginPlatform plugin platform} was added
         */
        public boolean addIfAbsent(@NotNull PluginPlatform... pluginPlatforms) {
            final Set<PluginPlatform> toAdd = Arrays.stream(pluginPlatforms)
                    .filter(filter -> get(filter.platform) == null)
                    .collect(Collectors.toSet());
            return this.pluginPlatforms.addAll(toAdd);
        }

        /**
         * Removes the {@link PluginPlatform plugin platforms} for the given {@link Platform platforms}
         *
         * @param   platforms   the {@link Platform platforms} to remove the {@link PluginPlatform plugin platforms} for
         *
         * @return              whether a {@link PluginPlatform plugin platform} was removed
         */
        public boolean remove(@NotNull Platform... platforms) {
            final List<Platform> toRemove = Arrays.asList(platforms);
            return pluginPlatforms.removeIf(filter -> toRemove.contains(filter.platform));
        }

        @Override @NotNull
        public String toString() {
            return pluginPlatforms.toString();
        }
    }
}
