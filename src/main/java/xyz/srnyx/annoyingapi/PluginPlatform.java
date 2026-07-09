package xyz.srnyx.annoyingapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import xyz.srnyx.annoyingapi.utility.ConfigurationUtility;
import xyz.srnyx.javautilities.manipulation.Mapper;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.*;
import java.util.logging.Level;


/**
 * Contains information about a plugin on a {@link Platform}
 */
public class PluginPlatform extends Stringable implements Comparable<PluginPlatform> {
    /**
     * The platform the plugin is on
     */
    @NotNull public final Platform platform;
    /**
     * The identifier of the plugin on the platform
     */
    @NotNull public final String identifier;

    /**
     * Creates a new {@link PluginPlatform}
     *
     * @param   platform    {@link #platform}
     * @param   identifier  {@link #identifier}
     */
    public PluginPlatform(@NotNull Platform platform, @NotNull String identifier) {
        this.platform = platform;
        this.identifier = identifier;
    }

    @Override
    public int compareTo(@NonNull PluginPlatform o) {
        return platform.compareTo(o.platform);
    }

    /**
     * Loads a {@link PluginPlatform} from a {@link ConfigurationSection}
     *
     * @param   section the section to load from
     *
     * @return          the loaded {@link PluginPlatform}
     */
    @NotNull
    public static Optional<PluginPlatform> load(@NotNull ConfigurationSection section) {
        // platform
        final String name = section.getName();
        final String platformName = name.isEmpty() ? section.getString("platform") : name;
        if (platformName == null) {
            AnnoyingPlugin.log(Level.WARNING, "&6platform&e is null for section &6" + section.getCurrentPath());
            return Optional.empty();
        }
        final Platform platform = Mapper.toEnum(platformName, Platform.class).orElse(null);
        if (platform == null) {
            AnnoyingPlugin.log(Level.WARNING, "&eInvalid platform &6" + platformName + "&e for section &6" + section.getCurrentPath());
            return Optional.empty();
        }

        // identifier
        final String identifier = section.getString("identifier");
        if (identifier == null) {
            AnnoyingPlugin.log(Level.WARNING, "&eidentifier&e is null for platform &6" + platform.name());
            return Optional.empty();
        }

        return Optional.of(new PluginPlatform(platform, identifier));
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
     *
     * @return              a new {@link PluginPlatform}
     */
    @NotNull
    public static PluginPlatform hangar(@NotNull String identifier) {
        return new PluginPlatform(Platform.HANGAR, identifier);
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
         * <p><b>Example:</b> {@code srnyx/AnnoyingAPI}
         */
        HANGAR,
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

    /**
     * A collection of {@link PluginPlatform}s
     */
    @SuppressWarnings("UnusedReturnValue")
    public static class Multi extends TreeSet<PluginPlatform> {
        /**
         * Creates a new empty {@link Multi}
         */
        public Multi() {
            // Creates a new empty Multi
        }

        /**
         * Creates a new {@link Multi} with the given {@link PluginPlatform}s
         */
        public Multi(@NotNull Collection<PluginPlatform> pluginPlatforms) {
            super(pluginPlatforms);
        }

        /**
         * Creates a new {@link Multi} with the given {@link PluginPlatform}s
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

            // Map list (legacy, deprecated)
            final ConfigurationSection platformsSection = section.getConfigurationSection(key);
            if (platformsSection == null) {
                ConfigurationUtility.toConfigurationList(section.getMapList(key)).stream()
                        .map(PluginPlatform::load)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(multi::add);
                return multi;
            }

            // Key list
            for (final String platformKey : platformsSection.getKeys(false)) {
                final ConfigurationSection platformSection = platformsSection.getConfigurationSection(platformKey);

                // String
                if (platformSection == null) {
                    final String identifier = platformsSection.getString(platformKey);
                    if (identifier != null) Mapper.toEnum(platformKey, Platform.class).ifPresent(platform -> multi.add(new PluginPlatform(platform, identifier)));
                    continue;
                }

                // Section
                PluginPlatform.load(platformSection).ifPresent(multi::add);
            }

            return multi;
        }

        /**
         * Loads a {@link Multi} from the given {@link JsonObject}
         */
        @NotNull
        public static TreeSet<PluginPlatform> load(@NotNull JsonObject json) {
            final TreeSet<PluginPlatform> platforms = new TreeSet<>();
            for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
                Mapper.toEnum(entry.getKey(), Platform.class)
                        .ifPresent(platform -> Mapper.convertJsonElement(entry.getValue(), JsonPrimitive.class)
                                .flatMap(primitive -> Mapper.convertJsonPrimitive(primitive, String.class))
                                .ifPresent(identifier -> platforms.add(new PluginPlatform(platform, identifier))));
            }
            return platforms;
        }

        /**
         * Gets the {@link PluginPlatform} for the given {@link Platform}
         *
         * @param   platform    the {@link Platform} to get the {@link PluginPlatform} for
         *
         * @return              the {@link PluginPlatform} for the given {@link Platform}
         */
        @NotNull
        public Optional<PluginPlatform> get(@NotNull Platform platform) {
            return stream()
                    .filter(filter -> filter.platform == platform)
                    .findFirst();
        }

        /**
         * Gets the {@link PluginPlatform#identifier} for the given {@link Platform}
         *
         * @param   platform    the {@link Platform} to get the {@link PluginPlatform#identifier} for
         *
         * @return              the {@link PluginPlatform#identifier} for the given {@link Platform}
         */
        @NotNull
        public Optional<String> getIdentifier(@NotNull Platform platform) {
            return get(platform).map(filter -> filter.identifier);
        }

        /**
         * Removes the {@link PluginPlatform} for the given {@link Platform platforms}
         *
         * @param   platforms   the {@link Platform} to remove the {@link PluginPlatform plugin platforms} for
         *
         * @return              whether a {@link PluginPlatform plugin platform} was removed
         */
        public boolean remove(@NotNull Platform platforms) {
            return removeIf(filter -> filter.platform == platforms);
        }
    }
}
