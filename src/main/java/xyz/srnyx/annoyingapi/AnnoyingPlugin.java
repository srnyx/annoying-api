package xyz.srnyx.annoyingapi;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.relocation.Relocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.cooldown.CooldownManager;
import xyz.srnyx.annoyingapi.data.storage.ConnectionException;
import xyz.srnyx.annoyingapi.data.storage.DataManager;
import xyz.srnyx.annoyingapi.data.ItemData;
import xyz.srnyx.annoyingapi.data.storage.StorageConfig;
import xyz.srnyx.annoyingapi.data.storage.dialects.SQLDialect;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.events.AdvancedPlayerMoveEvent;
import xyz.srnyx.annoyingapi.events.PlayerDamageByPlayerEvent;
import xyz.srnyx.annoyingapi.file.AnnoyingFile;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.options.*;
import xyz.srnyx.annoyingapi.parents.Registrable;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;

import xyz.srnyx.javautilities.MapGenerator;
import xyz.srnyx.javautilities.objects.SemanticVersion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Represents a plugin using Annoying API
 */
@SuppressWarnings("EmptyMethod")
public class AnnoyingPlugin extends JavaPlugin {
    /**
     * The {@link Logger} for the plugin
     * <br>Uses temporary initialization until the {@link AnnoyingPlugin plugin} is constructed (loaded)
     */
    @NotNull public static Logger LOGGER = new Logger("AnnoyingAPI - ?", null) {
        @Override
        public void log(@NotNull LogRecord logRecord) {
            logRecord.setMessage("[AnnoyingAPI - ?] " + logRecord.getMessage());
            super.log(logRecord);
        }
    };
    /**
     * The Minecraft version the server is running
     */
    @NotNull public static final SemanticVersion MINECRAFT_VERSION = new SemanticVersion(Bukkit.getVersion().split("MC: ")[1].split("\\)")[0]);

    /**
     * The API options for the plugin
     */
    @NotNull public final AnnoyingOptions options = AnnoyingOptions.load(getResource("plugin.yml"));
    /**
     * The {@link BukkitLibraryManager} for the plugin
     */
    @NotNull public final BukkitLibraryManager libraryManager = new BukkitLibraryManager(this, "libs");
    /**
     * Set of loaded {@link RuntimeLibrary libraries}
     * <br>If a library's files are manually deleted (or something else goes wrong), the plugin will not notice this change until the library is {@link RuntimeLibrary#load(AnnoyingPlugin) loaded again} (usually requires a server restart)
     * <br>This should really only be used if you want to do a basic check if a library is loaded or not based on what features the API is currently using
     * <br>Something like {@link ItemData#attemptItemNbtApi(Supplier)} may be a better method when using a runtime library
     */
    @NotNull public final Set<RuntimeLibrary> loadedLibraries = new HashSet<>();
    /**
     * Wrapper for bStats
     */
    @Nullable public AnnoyingStats stats;
    /**
     * The {@link AnnoyingResource} that contains the plugin's messages
     */
    @Nullable public AnnoyingResource messages;
    /**
     * {@link ChatColor} aliases for the plugin from the messages file ({@link MessagesOptions.MessageKeys#globalPlaceholders})
     *
     * @see MessagesOptions.MessageKeys#globalPlaceholders
     */
    @NotNull public final Map<String, String> globalPlaceholders = new HashMap<>();
    /**
     * The {@link DataManager} for the plugin
     */
    @Nullable public DataManager dataManager;
    /**
     * Set of registered {@link Registrable}s by the plugin
     */
    @NotNull public final Set<Registrable> registeredClasses = new HashSet<>();
    /**
     * Custom events/listeners for the API
     */
    @NotNull public final Map<Class<? extends Event>, AnnoyingListener> customEvents = MapGenerator.HASH_MAP.mapOf(
            AdvancedPlayerMoveEvent.class, new AdvancedPlayerMoveEvent.Handler(this),
            PlayerDamageByPlayerEvent.class, new PlayerDamageByPlayerEvent.Handler(this));
    /**
     * The {@link CooldownManager} for the plugin
     */
    @NotNull public final CooldownManager cooldownManager = new CooldownManager(this);
    /**
     * Whether PlaceholderAPI is installed
     */
    public boolean papiInstalled = false;

    /**
     * Constructs a new {@link AnnoyingPlugin} instance
     */
    public AnnoyingPlugin() {
        LOGGER = getLogger();
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
        loadDataManger(false);
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
        // Get missing dependencies
        final List<AnnoyingDependency> missingDependencies = new ArrayList<>();
        for (final AnnoyingDependency dependency : options.pluginOptions.dependencies) if (dependency.isNotInstalled() && missingDependencies.stream().noneMatch(dep -> dep.name.equals(dependency.name))) missingDependencies.add(dependency);

        // Download missing dependencies then enable the plugin
        if (!missingDependencies.isEmpty()) {
            log(Level.WARNING, "&6&lMissing dependencies! &eAnnoying API will attempt to download/install them...");
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
        if (dataManager != null && dataManager.storageConfig.cache.saveOn.contains(StorageConfig.SaveOn.DISABLE)) dataManager.saveCache();
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
                .collect(Collectors.joining("&c, &4"));
        if (!missing.isEmpty()) {
            log(Level.SEVERE, "&cDisabling &4" + getName() + "&c because it's missing required dependencies: &4" + missing);
            disablePlugin();
            return;
        }

        // Enable bStats
        if (new AnnoyingResource(this, options.bStatsOptions.fileName, options.bStatsOptions.fileOptions).getBoolean(options.bStatsOptions.toggleKey)) {
            RuntimeLibrary.BSTATS_BASE.load(this);
            RuntimeLibrary.BSTATS_BUKKIT.load(this);
            stats = new AnnoyingStats(this);
        }

        // Get start message colors
        final String primaryColorString = globalPlaceholders.get("p");
        final String primaryColor = primaryColorString != null ? BukkitUtility.color(primaryColorString) : ChatColor.AQUA.toString();
        final String secondaryColorString = globalPlaceholders.get("s");
        final String secondaryColor = secondaryColorString != null ? BukkitUtility.color(secondaryColorString) : ChatColor.DARK_AQUA.toString();

        // Get start messages
        final PluginDescriptionFile description = getDescription();
        final String nameVersion = getName() + " v" + description.getVersion();
        final String authors = "By " + String.join(", ", description.getAuthors());
        final StringBuilder lineBuilder = new StringBuilder(secondaryColor);
        final int lineLength = Math.max(nameVersion.length(), authors.length());
        for (int i = 0; i < lineLength; i++) lineBuilder.append("-");
        final String line = lineBuilder.toString();

        // Send start messages
        log(Level.INFO, line);
        log(Level.INFO, primaryColor + nameVersion);
        log(Level.INFO, primaryColor + authors);
        log(Level.INFO, line);

        // Check for updates
        checkUpdate();

        // Register manually-defined Registrables & PAPI expansion
        options.registrationOptions.toRegister.forEach(Registrable::register);
        papiInstalled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (papiInstalled) options.registrationOptions.getPapiExpansionToRegister().ifPresent(PlaceholderExpansion::register);

        // Automatic registration
        final Set<String> packages = options.registrationOptions.automaticRegistration.packages;
        if (!packages.isEmpty()) {
            // Load Javassist and Reflections libraries
            RuntimeLibrary.JAVASSIST.load(this);
            RuntimeLibrary.REFLECTIONS.load(this);

            // Register classes
            final Set<Class<? extends Registrable>> ignoredClasses = options.registrationOptions.automaticRegistration.ignoredClasses;
            AnnoyingReflections.getSubTypesOf(packages, Registrable.class).stream()
                    .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()) && !ignoredClasses.contains(clazz))
                    .forEach(clazz -> {
                        try {
                            clazz.getConstructor(this.getClass()).newInstance(this).register();
                        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            log(Level.WARNING, "&eFailed to register &6" + clazz.getSimpleName());
                            e.printStackTrace();
                        }
                    });
        }

        // Start cache saving on interval if enabled
        if (dataManager != null && dataManager.storageConfig.cache.saveOn.contains(StorageConfig.SaveOn.INTERVAL)) dataManager.startCacheSavingOnInterval(this, dataManager.storageConfig.cache.interval);

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
        loadDataManger(dataManager != null && dataManager.storageConfig.cache.saveOn.contains(StorageConfig.SaveOn.RELOAD));
        reload();
    }

    /**
     * Loads the messages.yml file to {@link #messages} and {@link #globalPlaceholders}
     */
    public void loadMessages() {
        messages = new AnnoyingResource(this, options.messagesOptions.fileName, options.messagesOptions.fileOptions);
        // globalPlaceholders
        globalPlaceholders.clear();
        final ConfigurationSection section = messages.getConfigurationSection(options.messagesOptions.keys.globalPlaceholders);
        if (section != null) section.getKeys(false).forEach(key -> globalPlaceholders.put(key, section.getString(key)));
    }

    /**
     * Disables the plugin. Unregisters commands/listeners, cancels tasks, and then runs {@link PluginManager#disablePlugin(Plugin)}
     * <p><i>This is not meant to be overriden, only override if you know what you're doing!</i>
     */
    public void disablePlugin() {
        new HashSet<>(registeredClasses).forEach(Registrable::unregister);
        Bukkit.getScheduler().cancelTasks(this);
        if (dataManager != null) dataManager.saveCache();
        Bukkit.getPluginManager().disablePlugin(this);
    }

    /**
     * Gets a string from {@link #messages} with the specified key
     *
     * @param   key the key of the string
     *
     * @return      the string, or the {@code key} if {@link #messages} is {@code null} or the string is not found
     */
    @NotNull
    public String getMessagesString(@NotNull String key) {
        return messages != null ? messages.getString(key, key) : key;
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
        return papiInstalled ? PlaceholderAPI.setPlaceholders(player, message) : message;
    }

    /**
     * Unregisters all {@link Registrable}s in {@link #registeredClasses}
     */
    public void unregisterClasses() {
        new HashSet<>(registeredClasses).forEach(Registrable::unregister);
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
     * Attempts to load the {@link #dataManager}, catching any exceptions and logging them
     * <br>If {@code storage-new.yml} exists, it will attempt to migrate the data from {@code storage.yml} to {@code storage-new.yml} using {@link #attemptDatabaseMigration(DataManager)}
     *
     * @param   saveCache   whether to save the cache before loading the data manager
     *                      <br><i>Data may be lost if {@code false}!</i>
     */
    public void loadDataManger(boolean saveCache) {
        // Check if a manager is already loaded
        if (dataManager != null) {
            // Save cache
            if (saveCache) dataManager.saveCache();
            // Close previous connection
            try {
                dataManager.connection.close();
            } catch (final SQLException e) {
                log(Level.SEVERE, "&cFailed to close the database connection, it's recommended to restart the server!", e);
            }
        }

        // Cancel if data is disabled
        if (!options.dataOptions.enabled) {
            dataManager = null;
            return;
        }

        // Connect to database
        try {
            dataManager = new DataManager(new AnnoyingResource(this, "storage.yml"));
        } catch (final ConnectionException e) {
            dataManager = null;
            log(Level.SEVERE, "&4storage.yml &8|&c Failed to connect to database! URL: '&4" + e.url + "&c' Properties: &4" + e.getPropertiesRedacted(), e);
            return;
        }

        // Attempt database migration
        dataManager = attemptDatabaseMigration(dataManager);

        // Create tables/columns
        dataManager.createTablesColumns(options.dataOptions.tables);
    }

    /**
     * Attempts to migrate data from {@code storage.yml} to {@code storage-new.yml}
     *
     * @param   oldManager  the old data manager
     *
     * @return              the new data manager
     */
    @NotNull
    private DataManager attemptDatabaseMigration(@NotNull DataManager oldManager) {
        // Check if migration is needed (storage-new.yml exists)
        final File dataFolder = getDataFolder();
        final File storageNew = new File(dataFolder, "storage-new.yml");
        if (!storageNew.exists()) return oldManager;
        log(Level.INFO, "&aFound &2storage-new.yml&a, attempting to migrate data from &2storage.yml&a to &2storage-new.yml&a...");

        // NEW: Connect to new database
        final AnnoyingFile<?> storageNewFile = new AnnoyingFile<>(this, storageNew, new AnnoyingFile.Options<>().canBeEmpty(false));
        if (!storageNewFile.load()) return oldManager;
        final DataManager newManager;
        try {
            newManager = new DataManager(storageNewFile);
        } catch (final ConnectionException e) {
            log(Level.SEVERE, "&4storage-new.yml &8|&c Failed to connect to database! URL: '&4" + e.url + "&c' Properties: &4" + e.getPropertiesRedacted(), e);
            return oldManager;
        }

        // OLD: Get tables & columns
        final Set<String> tables = new HashSet<>();
        try (final PreparedStatement getTables = oldManager.dialect.getTables()) {
            final ResultSet resultSet = getTables.executeQuery();
            while (resultSet.next()) tables.add(resultSet.getString(1));
        } catch (final SQLException e) {
            log(Level.SEVERE, "&4" + newManager.storageConfig.getMigrationName() + " &8|&c Failed to get tables!", e);
            return oldManager;
        }

        // OLD: Get values
        final Map<String, Set<String>> tablesColumns = new HashMap<>(); // {Table, Columns}
        final Map<String, Map<String, Map<String, String>>> values = new HashMap<>(); // {Table, {Target, {Column, Value}}}
        final int oldPrefixLength = oldManager.tablePrefix.length();
        for (final String table : tables) {
            final String tableWithoutPrefix = table.substring(oldPrefixLength);
            final Map<String, Map<String, String>> tableValues = new HashMap<>(); // {Target, {Column, Value}}
            try (final PreparedStatement getValues = oldManager.dialect.getValues(table)) {
                final ResultSet resultSet = getValues.executeQuery();

                // Continue if table doesn't have target column
                final Set<String> columns = new HashSet<>();
                final ResultSetMetaData metaData = resultSet.getMetaData();
                final int columnCount = metaData.getColumnCount();
                if (columnCount == 0) {
                    log(Level.WARNING, "&4" + oldManager.storageConfig.getMigrationName() + " &8|&c Table &4" + table + "&c has no columns, skipping...");
                    continue;
                }
                for (int i = 1; i <= columnCount; i++) columns.add(metaData.getColumnName(i));
                if (!columns.contains("target")) {
                    log(Level.WARNING, "&4" + oldManager.storageConfig.getMigrationName() + " &8|&c Table &4" + table + "&c doesn't have a '&4target&c' column, skipping...");
                    continue;
                }
                tablesColumns.put(tableWithoutPrefix, columns);

                // Get values for each target
                while (resultSet.next()) {
                    final String target = resultSet.getString("target");
                    if (target == null) continue;
                    final Map<String, String> columnValues = new HashMap<>(); // {Column, Value}
                    for (final String column : columns) if (!column.equals("target")) columnValues.put(column, resultSet.getString(column));
                    tableValues.put(target, columnValues);
                }
            } catch (final SQLException e) {
                log(Level.SEVERE, "&4" + oldManager.storageConfig.getMigrationName() + " &8|&c Failed to get values for table &4" + table, e);
            }
            if (!tableValues.isEmpty()) values.put(newManager.getTableName(tableWithoutPrefix), tableValues);
        }

        if (!values.isEmpty()) {
            // NEW: Create missing tables/columns
            newManager.createTablesColumns(tablesColumns);

            // NEW: Save values to new database
            for (final SQLDialect.SetValueStatement statement : newManager.dialect.setValues(values)) try {
                statement.statement.executeUpdate();
            } catch (final SQLException e) {
                log(Level.SEVERE, "&4" + newManager.storageConfig.getMigrationName() + " &8|&c Failed to migrate values for &4" + statement.target + "&c in table &4" + statement.table + "&c: &4" + statement.values, e);
            }
        } else {
            log(Level.WARNING, "&4" + oldManager.storageConfig.getMigrationName() + " &8|&c Found no data to migrate! This may or may not be an error...");
        }

        // OLD: Close old connection
        try {
            oldManager.connection.close();
        } catch (final SQLException e) {
            log(Level.SEVERE, "&cFailed to close the old database connection, it's recommended to restart the server!", e);
        }

        // PREV: Delete storage-old.yml if it exists (from a previous migration)
        final File storageOld = new File(dataFolder, "storage-old.yml");
        if (storageOld.exists()) try {
            Files.delete(storageOld.toPath());
        } catch (final IOException e) {
            log(Level.SEVERE, "&cFailed to delete previous &4storage-old.yml!");
        }

        // Rename files
        final File storage = oldManager.storageConfig.file.file;
        if (storage.renameTo(storageOld)) { // OLD: storage.yml -> storage-old.yml
            if (!storageNew.renameTo(storage)) { // NEW: storage-new.yml -> storage.yml
                log(Level.SEVERE, "Failed to rename &4storage-new.yml&c to &4storage.yml&c! You MUST rename &4storage-new.yml&c to &4storage.yml&c manually!");
            }
        } else {
            log(Level.SEVERE, "Failed to rename &4storage.yml&c to &4storage-old.yml&c! You MUST rename &4storage.yml&c to &4storage-old.yml&c and &4storage-new.yml&c to &4storage.yml&c manually!");
        }

        // NEW: Use new storage
        log(Level.INFO, "&aFinished migrating data from &2storage.yml&a to &2storage-new.yml&a!");
        return newManager;
    }

    /**
     * Attempt to run a task asynchronously if the plugin is enabled, otherwise run it synchronously
     *
     * @param   runnable    the task to run
     *
     * @return              {@code true} if the task was run asynchronously, {@code false} if it was run synchronously
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean attemptAsync(@NotNull Runnable runnable) {
        try {
            Bukkit.getScheduler().runTaskAsynchronously(this, runnable);
            return true;
        } catch (final IllegalPluginAccessException e) {
            runnable.run();
            return false;
        }
    }

    /**
     * Gets a {@link Relocation} for the specified package
     *
     * @param   from    the package to relocate
     * @param   name    the name of the module being relocated ({@link #getLibsPackage()} + {@code name})
     *
     * @return          the relocation
     */
    @NotNull
    public Relocation getRelocation(@NotNull String from, @NotNull String name) {
        return new Relocation(from, getLibsPackage() + name);
    }

    /**
     * Gets a {@link Relocation} for the specified package
     *
     * @param   from    the package to relocate
     *
     * @return          the relocation
     */
    @NotNull
    public Relocation getRelocation(@NotNull String from) {
        return getRelocation(from, from.substring(from.lastIndexOf("{}") + 2).toLowerCase().replaceAll("[^a-z0-9._{}]", ""));
    }

    /**
     * Gets the package path for relocated libraries
     *
     * @return  the path
     */
    @NotNull
    public String getLibsPackage() {
        return getClass().getPackage().getName() + "{}libs{}";
    }

    /**
     * Logs a message with the specified level and throwable to the console
     *
     * @param   level       the level of the message. If {@code null}, {@link Level#INFO} will be used
     * @param   message     the message to log
     * @param   throwable   the throwable to log
     */
    public static void log(@Nullable Level level, @Nullable Object message, @Nullable Throwable throwable) {
        if (level == null) level = Level.INFO;
        // Only color the message if the server version is between 1.12 and 1.20
        LOGGER.log(level, MINECRAFT_VERSION.isGreaterThanOrEqualTo(1, 12, 0) && MINECRAFT_VERSION.isLessThanOrEqualTo(1, 20, 0) ? BukkitUtility.color(message) : BukkitUtility.stripUntranslatedColor(String.valueOf(message)), throwable);
    }

    /**
     * Calls {@link #log(Level, Object, Throwable)} with {@code null} as the {@link Throwable throwable}
     *
     * @param   level   the level of the message. If {@code null}, {@link Level#INFO} will be used
     * @param   message the message to log
     */
    public static void log(@Nullable Level level, @Nullable Object message) {
        log(level, message, null);
    }

    /**
     * Calls {@link #log(Level, Object, Throwable)} with {@code null} as the {@link Level level} and {@link Throwable throwable}
     *
     * @param   message the message to log
     */
    public static void log(@Nullable Object message) {
        log(null, message, null);
    }
}
