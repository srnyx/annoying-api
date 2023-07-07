package xyz.srnyx.annoyingapi;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.reflections.Reflections;

import xyz.srnyx.annoyingapi.command.AnnoyingCommand;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.events.EventHandlers;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.file.MessagesFormat;
import xyz.srnyx.annoyingapi.options.AnnoyingOptions;
import xyz.srnyx.annoyingapi.options.MessagesOptions;
import xyz.srnyx.annoyingapi.options.PluginOptions;
import xyz.srnyx.annoyingapi.parents.Registrable;
import xyz.srnyx.annoyingapi.utility.adventure.AdventureUtility;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Represents a plugin using the API
 */
@SuppressWarnings("EmptyMethod")
public class AnnoyingPlugin extends JavaPlugin {
    /**
     * The {@link Logger} for the plugin
     */
    public static ComponentLogger LOGGER;
    /**
     * The Minecraft version the server is running
     */
    @NotNull public static final SemanticVersion MINECRAFT_VERSION = new SemanticVersion(Bukkit.getVersion().split("MC: ")[1].split("\\)")[0]);

    /**
     * The API options for the plugin
     */
    @NotNull public final AnnoyingOptions options = AnnoyingOptions.load(new InputStreamReader(getResource("plugin.yml")));
    /**
     * The {@link Metrics bStats} instance for the plugin
     */
    @Nullable public Metrics bStats;
    /**
     * The {@link AnnoyingResource} that contains the plugin's messages
     */
    @Nullable public AnnoyingResource messages;
    /**
     * The {@link MessagesFormat} of the {@link #messages} file
     */
    @NotNull public MessagesFormat messagesFormat = MessagesFormat.LEGACY;
    /**
     * {@link ChatColor} aliases for the plugin from the messages file ({@link MessagesOptions.MessageKeys#globalPlaceholders})
     *
     * @see MessagesOptions.MessageKeys#globalPlaceholders
     */
    @NotNull public final Map<String, Component> globalPlaceholders = new HashMap<>();
    /**
     * The {@link BukkitAudiences} instance for the plugin
     */
    public BukkitAudiences audiences;
    /**
     * Set of registered {@link AnnoyingCommand}s by the plugin
     */
    @NotNull public final Set<AnnoyingCommand> registeredCommands = new HashSet<>();
    /**
     * Set of registered {@link AnnoyingListener}s by the plugin
     */
    @NotNull public final Set<AnnoyingListener> registeredListeners = new HashSet<>();
    /**
     * Stores the cooldowns for each player/type
     */
    @NotNull public final Map<UUID, Map<AnnoyingCooldown.CooldownType, Long>> cooldowns = new HashMap<>();
    /**
     * Whether PlaceholderAPI is installed
     */
    public boolean papiInstalled = false;

    /**
     * Constructs a new {@link AnnoyingPlugin} instance. Registers event handlers for custom events
     */
    public AnnoyingPlugin() {
        LOGGER = ComponentLogger.logger(getName());
        options.registrationOptions.listenersToRegister.add(new EventHandlers(this));
    }

    /**
     * Called after a plugin is loaded but before it has been enabled.
     * When multiple plugins are loaded, the onLoad() for all plugins is called before any onEnable() is called.
     * <p><b>Do not try to override this method! Override {@link #load()} instead</b>
     *
     * @see #load()
     */
    @Override
    public final void onLoad() {
        loadMessages();
        load();
    }

    /**
     * Called when the plugin is enabled.
     * <p><b>Do not try to override this method! Override {@link #enable()} instead</b>
     *
     * @see #enable()
     */
    @Override
    public final void onEnable() {
        audiences = BukkitAudiences.create(this); // Must be initialized in onEnable as it registers a listener

        // Get missing dependencies
        final List<AnnoyingDependency> missingDependencies = new ArrayList<>();
        for (final AnnoyingDependency dependency : options.pluginOptions.dependencies) if (dependency.isNotInstalled() && missingDependencies.stream().noneMatch(dep -> dep.name.equals(dependency.name))) missingDependencies.add(dependency);

        // Download missing dependencies then enable the plugin
        if (!missingDependencies.isEmpty()) {
            log(Level.WARNING, Component.text("Missing dependencies!", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .append(Component.text(" Annoying API will attempt to download/install them...", NamedTextColor.YELLOW)));
            new AnnoyingDownload(this, missingDependencies).downloadPlugins(this::enablePlugin);
            return;
        }

        // Enable the plugin immediately if there are no missing dependencies
        enablePlugin();
    }

    /**
     * Called when the plugin is disabled
     * <p><b>Do not try to override this method! Override {@link #disable()} instead</b>
     *
     * @see #disable()
     */
    @Override
    public final void onDisable() {
        disable();
    }

    /**
     * Called when the plugin is loaded
     */
    public void load() {
        // This method is meant to be overridden
    }

    /**
     * Called after dependency checks, start-up messages, and command/listener registration.
     */
    public void enable() {
        // This method is meant to be overridden
    }

    /**
     * Called when the plugin is disabled
     */
    public void disable() {
        // This method is meant to be overridden
    }

    /**
     * Called when the plugin is reloaded
     *
     * @see #reloadPlugin()
     */
    public void reload() {
        // This method is meant to be overridden
    }

    /**
     * Plugin enabling stuff
     * <b><p>Do not try to override this method! Override {@link #enable()} instead</b>
     *
     * @see #enable()
     */
    private void enablePlugin() {
        // Check if required dependencies are installed
        final String missing = options.pluginOptions.dependencies.stream()
                .filter(dependency -> dependency.required && dependency.isNotInstalled())
                .map(dependency -> dependency.name)
                .collect(Collectors.joining("<red>, <dark_red>"));
        if (!missing.isEmpty()) {
            log(Level.SEVERE, Component.text("Disabling ", NamedTextColor.RED)
                    .append(Component.text(getName(), NamedTextColor.DARK_RED))
                    .append(Component.text(" because it's missing required dependencies: ", NamedTextColor.RED))
                    .append(AdventureUtility.convertMiniMessage("<dark_red>" + missing)));
            disablePlugin();
            return;
        }

        // Enable bStats
        if (new AnnoyingResource(this, options.bStatsOptions.fileName, options.bStatsOptions.fileOptions).getBoolean("enabled")) {
            final Metrics metrics = new Metrics(this, 18281); // API
            metrics.addCustomChart(new SimplePie("plugins", this::getName));
            metrics.addCustomChart(new SimplePie("messages_format", () -> messagesFormat.statsName));
            if (options.bStatsOptions.id != null) bStats = new Metrics(this, options.bStatsOptions.id); // Plugin
        }

        // Send start messages
        final PluginDescriptionFile description = getDescription();
        final String nameVersion = getName() + " v" + description.getVersion();
        final String authors = "By " + String.join(", ", description.getAuthors());
        final int lineLength = Math.max(nameVersion.length(), authors.length());
        if (messagesFormat.isMiniMessage()) {
            // MiniMessage start messages
            final Style primaryColor = globalPlaceholders.getOrDefault("p", Component.text("", NamedTextColor.AQUA)).style();
            final TextComponent.Builder lineBuilder = Component.text();
            for (int i = 0; i < lineLength; i++) lineBuilder.append(Component.text("-"));
            final TextComponent line = lineBuilder.style(globalPlaceholders.getOrDefault("s", Component.text("", NamedTextColor.DARK_GRAY)).style()).build();

            // Send
            log(line);
            log(Component.text(nameVersion, primaryColor));
            log(Component.text(authors, primaryColor));
            log(line);
        } else {
            // Legacy start messages
            final Component pPlaceholder = globalPlaceholders.get("p");
            final String primaryColor = pPlaceholder != null ? AdventureUtility.convertLegacy(pPlaceholder) : ChatColor.AQUA.toString();
            final Component sPlaceholder = globalPlaceholders.get("s");
            final StringBuilder lineBuilder = new StringBuilder(sPlaceholder != null ? AdventureUtility.convertLegacy(sPlaceholder) : ChatColor.DARK_AQUA.toString());
            for (int i = 0; i < lineLength; i++) lineBuilder.append("-");
            final String line = lineBuilder.toString();

            // Send
            log(line);
            log(primaryColor + nameVersion);
            log(primaryColor + authors);
            log(line);
        }

        // Check for updates
        checkUpdate();

        // Register manually-defined commands/listeners/PAPI expansion
        options.registrationOptions.commandsToRegister.forEach(AnnoyingCommand::register);
        options.registrationOptions.listenersToRegister.forEach(AnnoyingListener::register);
        papiInstalled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (papiInstalled) {
            final PlaceholderExpansion expansion = options.registrationOptions.getPapiExpansionToRegister();
            if (expansion != null) expansion.register();
        }

        // Automatic registration
        final Set<Class<? extends Registrable>> ignoredClasses = options.registrationOptions.automaticRegistration.ignoredClasses;
        options.registrationOptions.automaticRegistration.packages.stream()
                .map(packageName -> new Reflections(packageName).getSubTypesOf(Registrable.class).stream()
                        .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()) && !ignoredClasses.contains(clazz))
                        .collect(Collectors.toSet()))
                .flatMap(Set::stream)
                .forEach(clazz -> {
                    try {
                        clazz.getConstructor(this.getClass()).newInstance(this).register();
                    } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        log(Level.WARNING, "&eFailed to register &6" + clazz.getSimpleName());
                    }
                });

        // Custom onEnable
        enable();
    }

    /**
     * Reloads the plugin ({@link #messages}, etc...). This will not trigger {@link #onLoad()} or {@link #onEnable()}
     * <p>This is not run automatically (such as {@link #onEnable()} and {@link #onDisable()}), it is to be used manually by the plugin itself (ex: in a {@code /reload} command)
     * <p><b>Do not try to override this method! Override {@link #reload()} instead</b>
     *
     * @see #reload()
     */
    public void reloadPlugin() {
        loadMessages();
        reload();
    }

    /**
     * Loads the messages.yml file to {@link #messages} and {@link #globalPlaceholders}
     */
    public void loadMessages() {
        messages = new AnnoyingResource(this, options.messagesOptions.fileName, options.messagesOptions.fileOptions);
        // messagesFormat
        messagesFormat = MessagesFormat.fromString(messages.getString(options.messagesOptions.keys.format));
        // globalPlaceholders
        globalPlaceholders.clear();
        final ConfigurationSection section = messages.getConfigurationSection(options.messagesOptions.keys.globalPlaceholders);
        if (section != null) section.getKeys(false).forEach(key -> globalPlaceholders.put(key, getMessagesComponent(key)));
    }

    /**
     * Disables the plugin. Unregisters commands/listeners, cancels tasks, and then runs {@link PluginManager#disablePlugin(Plugin)}
     * <p><i>This is not meant to be overriden, only override if you know what you're doing!</i>
     */
    public void disablePlugin() {
        new HashSet<>(registeredCommands).forEach(AnnoyingCommand::unregister);
        new HashSet<>(registeredListeners).forEach(AnnoyingListener::unregister);
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @NotNull
    public Component getMessagesComponent(@NotNull String key) {
        if (messages == null) return Component.text(key);
        final String string = messages.getString(key);
        if (string == null) return Component.text(key);
        if (messagesFormat.isMiniMessage()) return AdventureUtility.convertMiniMessage(string);
        return AdventureUtility.convertLegacy(string);
    }

    /**
     * Parses all PlaceholderAPI placeholders in a message
     *
     * @param   player  the {@link OfflinePlayer} to parse the placeholders for (or {@code null} if none)
     * @param   message the message to parse
     *
     * @return          the parsed message
     */
    @NotNull
    public String parsePapiPlaceholders(@Nullable OfflinePlayer player, @Nullable String message) {
        if (message == null) return "null";
        if (!papiInstalled) return message;
        return PlaceholderAPI.setPlaceholders(player, message);
    }

    /**
     * Unregisters all {@link AnnoyingListener}s in {@link #registeredListeners}
     */
    public void unregisterListeners() {
        new HashSet<>(registeredListeners).forEach(AnnoyingListener::unregister);
    }

    /**
     * Unregisters all {@link AnnoyingCommand}s in {@link #registeredCommands}
     */
    public void unregisterCommands() {
        new HashSet<>(registeredCommands).forEach(AnnoyingCommand::unregister);
    }

    /**
     * Runs {@link AnnoyingUpdate#checkUpdate()} with {@link PluginOptions#updatePlatforms}
     *
     * @see AnnoyingUpdate
     */
    public void checkUpdate() {
        new AnnoyingUpdate(this, options.pluginOptions.updatePlatforms).checkUpdate();
    }

    /**
     * Logs a message to the console
     *
     * @param   level       the level of the message. If {@code null}, {@link Level#INFO} will be used
     * @param   component   the message to log
     */
    public static void log(@Nullable Level level, @Nullable Component component) {
        if (level == null) level = Level.INFO;
        if (component == null) component = Component.empty();

        if (level.equals(Level.WARNING)) {
            LOGGER.warn(component);
            return;
        }

        if (level.equals(Level.SEVERE)) {
            LOGGER.error(component);
            return;
        }

        LOGGER.info(component);
    }

    /**
     * Calls {@link #log(Level, Component)} with {@code null} as the {@link Level level}
     *
     * @param   component   the message to log
     */
    public static void log(@NotNull Component component) {
        log(null, component);
    }

    /**
     * Logs a message to the console
     *
     * @param   level   the level of the message. If {@code null}, {@link Level#INFO} will be used
     * @param   message the message to log
     */
    public static void log(@Nullable Level level, @Nullable String message) {
        log(level, AdventureUtility.convertMiniMessage(message));
    }

    /**
     * Calls {@link #log(Level, String)} with {@code null} as the {@link Level level}
     *
     * @param   message the message to log
     */
    public static void log(@NotNull String message) {
        log(null, message);
    }
}
