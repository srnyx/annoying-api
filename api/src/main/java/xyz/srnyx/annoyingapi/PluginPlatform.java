package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;


/**
 * Contains information about a plugin on a {@link Platform}
 */
public class PluginPlatform {
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
        if (platform.requiresAuthor) throw new IllegalArgumentException(platform + " requires an author");
    }

    /**
     * Creates a new {@link PluginPlatform}
     *
     * @param   platform    {@link #platform}
     * @param   identifier  {@link #identifier}
     * @param   author      {@link #author}
     */
    public PluginPlatform(@NotNull Platform platform, @NotNull String identifier, @NotNull String author) {
        this.platform = platform;
        this.identifier = identifier;
        this.author = author;
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
         * The {@link PluginPlatform}s in this {@link Multi}
         */
        @NotNull private final Collection<PluginPlatform> pluginPlatforms = new HashSet<>();

        /**
         * Creates a new {@link Multi} with the given {@link PluginPlatform}s
         *
         * @param   pluginPlatforms {@link #pluginPlatforms}
         */
        public Multi(@NotNull Collection<PluginPlatform> pluginPlatforms) {
            pluginPlatforms.forEach(this::putIfAbsent);
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
         * Adds the given {@link PluginPlatform} to this {@link Multi} if it doesn't already exist
         *
         * @param   pluginPlatform  the {@link PluginPlatform} to add
         */
        public void putIfAbsent(@NotNull PluginPlatform pluginPlatform) {
            if (get(pluginPlatform.platform) == null) pluginPlatforms.add(pluginPlatform);
        }

        /**
         * Removes the {@link PluginPlatform} for the given {@link Platform}
         *
         * @param   platform    the {@link Platform} to remove the {@link PluginPlatform} for
         */
        public void remove(@NotNull Platform platform) {
            pluginPlatforms.removeIf(filter -> filter.platform == platform);
        }
    }
}
