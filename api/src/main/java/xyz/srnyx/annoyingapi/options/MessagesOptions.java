package xyz.srnyx.annoyingapi.options;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.function.Consumer;


/**
 * Represents the options for the {@link AnnoyingPlugin#messages} file
 */
public class MessagesOptions extends Stringable {
    /**
     * <i>{@code OPTIONAL}</i>  The file name of the messages file <i>(usually {@code messages.yml})</i>
     * <p>If not specified, no messages will be loaded (plugin will still enable)
     */
    @NotNull public String fileName = "messages.yml";
    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingResource.Options options} for the {@link #fileName messages} file
     * <p>If not specified, the default options will be used
     */
    @Nullable public AnnoyingResource.Options fileOptions = null;
    /**
     * <i>{@code OPTIONAL}</i> The different message keys for some default messages in the {@link #fileName messages file}
     */
    @NotNull public MessageKeys keys = new MessageKeys();

    /**
     * Constructs a new {@link MessagesOptions} instance with default values
     */
    public MessagesOptions() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * Loads the options from the specified {@link ConfigurationSection}
     *
     * @param   section the section to load the options from
     *
     * @return          the loaded options
     */
    @NotNull
    public static MessagesOptions load(@NotNull ConfigurationSection section) {
        final MessagesOptions options = new MessagesOptions();
        if (section.contains("fileName")) options.fileName(section.getString("fileName"));
        if (section.contains("fileOptions")) options.fileOptions(AnnoyingResource.Options.load(section.getConfigurationSection("fileOptions")));
        if (section.contains("keys")) options.keys(MessageKeys.load(section.getConfigurationSection("keys")));
        return options;
    }

    /**
     * Sets {@link #fileName}
     *
     * @param   fileName    the new {@link #fileName}
     *
     * @return              the {@link MessagesOptions} instance for chaining
     */
    @NotNull
    public MessagesOptions fileName(@NotNull String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * Sets {@link #fileOptions}
     *
     * @param   fileOptions the new {@link #fileOptions}
     *
     * @return              the {@link MessagesOptions} instance for chaining
     */
    @NotNull
    public MessagesOptions fileOptions(@Nullable AnnoyingResource.Options fileOptions) {
        this.fileOptions = fileOptions;
        return this;
    }

    /**
     * Sets {@link #fileOptions} using the specified {@link Consumer}
     *
     * @param   consumer    the consumer to accept the new {@link #fileOptions}
     *
     * @return              the {@link MessagesOptions} instance for chaining
     */
    @NotNull
    public MessagesOptions fileOptions(@NotNull Consumer<AnnoyingResource.Options> consumer) {
        final AnnoyingResource.Options options = new AnnoyingResource.Options();
        consumer.accept(options);
        return fileOptions(options);
    }

    /**
     * Sets {@link #keys}
     *
     * @param   keys        the new {@link #keys}
     *
     * @return              the {@link MessagesOptions} instance for chaining
     */
    @NotNull
    public MessagesOptions keys(@NotNull MessageKeys keys) {
        this.keys = keys;
        return this;
    }

    /**
     * Sets {@link #keys}
     *
     * @param   consumer    the consumer to accept the new {@link #keys}
     *
     * @return              the {@link MessagesOptions} instance for chaining
     */
    @NotNull
    public MessagesOptions keys(@NotNull Consumer<MessageKeys> consumer) {
        consumer.accept(this.keys);
        return this;
    }

    /**
     * A class to hold the different default {@link AnnoyingPlugin#messages} keys
     */
    public static class MessageKeys extends Stringable {
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's format
         */
        @NotNull public String format = "plugin.format";
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's global placeholders
         *
         * @see AnnoyingPlugin#globalPlaceholders
         */
        @NotNull public String globalPlaceholders = "plugin.global-placeholders";
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's JSON component splitter
         */
        @NotNull public String splitterJson = "plugin.splitters.json";
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's placeholder component splitter
         */
        @NotNull public String splitterPlaceholder = "plugin.splitters.placeholder";
        /**
         * <i>{@code OPTIONAL}</i> The key for the message sent in the console when an update is available for the plugin
         */
        @NotNull public String updateAvailable = "plugin.update-available";
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "no permission" message
         */
        @NotNull public String noPermission = "error.no-permission";
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "player-only" message
         */
        @NotNull public String playerOnly = "error.player-only";
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "invalid argument" message
         * <p>This should contain {@code %argument%} for the invalid argument
         */
        @NotNull public String invalidArgument = "error.invalid-argument";
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "invalid arguments" message
         */
        @NotNull public String invalidArguments = "error.invalid-arguments";
        /**
         * <i>{@code OPTIONAL}</i> The {@link AnnoyingPlugin#messages} key for the plugin's "disabled command" message
         */
        @NotNull public String disabledCommand = "error.disabled-command";

        /**
         * Creates a new {@link MessageKeys} with the default values
         */
        public MessageKeys() {
            // Only exists to give the constructor a Javadoc
        }

        /**
         * Loads the {@link MessageKeys} from the specified {@link ConfigurationSection}
         *
         * @param section the section to load the {@link MessageKeys} from
         * @return the loaded {@link MessageKeys}
         */
        @NotNull
        public static MessagesOptions.MessageKeys load(@NotNull ConfigurationSection section) {
            final MessageKeys keys = new MessageKeys();
            if (section.contains("globalPlaceholders")) keys.globalPlaceholders(section.getString("globalPlaceholders"));
            if (section.contains("splitterJson")) keys.splitterJson(section.getString("splitterJson"));
            if (section.contains("splitterPlaceholder")) keys.splitterPlaceholder(section.getString("splitterPlaceholder"));
            if (section.contains("updateAvailable")) keys.updateAvailable(section.getString("updateAvailable"));
            if (section.contains("noPermission")) keys.noPermission(section.getString("noPermission"));
            if (section.contains("playerOnly")) keys.playerOnly(section.getString("playerOnly"));
            if (section.contains("invalidArgument")) keys.invalidArgument(section.getString("invalidArgument"));
            if (section.contains("invalidArguments")) keys.invalidArguments(section.getString("invalidArguments"));
            if (section.contains("disabledCommand")) keys.disabledCommand(section.getString("disabledCommand"));
            return keys;
        }

        /**
         * Sets {@link #format}
         *
         * @param   format      the new {@link #format}
         *
         * @return              the {@link MessageKeys} instance for chaining
         */
        @NotNull
        public MessageKeys format(@NotNull String format) {
            this.format = format;
            return this;
        }

        /**
         * Sets {@link #globalPlaceholders}
         *
         * @param   globalPlaceholders  the new {@link #globalPlaceholders}
         *
         * @return                      the {@link MessageKeys} instance for chaining
         */
        @NotNull
        public MessageKeys globalPlaceholders(@NotNull String globalPlaceholders) {
            this.globalPlaceholders = globalPlaceholders;
            return this;
        }

        /**
         * Sets {@link #splitterJson}
         *
         * @param   splitterJson    the new {@link #splitterJson}
         *
         * @return                  the {@link MessageKeys} instance for chaining
         */
        @NotNull
        public MessageKeys splitterJson(@NotNull String splitterJson) {
            this.splitterJson = splitterJson;
            return this;
        }

        /**
         * Sets {@link #splitterPlaceholder}
         *
         * @param   splitterPlaceholder the new {@link #splitterPlaceholder}
         *
         * @return                      the {@link MessageKeys} instance for chaining
         */
        @NotNull
        public MessageKeys splitterPlaceholder(@NotNull String splitterPlaceholder) {
            this.splitterPlaceholder = splitterPlaceholder;
            return this;
        }

        /**
         * Sets {@link #updateAvailable}
         *
         * @param   updateAvailable the new {@link #updateAvailable}
         *
         * @return                  the {@link MessageKeys} instance for chaining
         */
        @NotNull
        public MessageKeys updateAvailable(@NotNull String updateAvailable) {
            this.updateAvailable = updateAvailable;
            return this;
        }

        /**
         * Sets {@link #noPermission}
         *
         * @param   noPermission    the new {@link #noPermission}
         *
         * @return                  the {@link MessageKeys} instance for chaining
         */
        @NotNull
        public MessageKeys noPermission(@NotNull String noPermission) {
            this.noPermission = noPermission;
            return this;
        }

        /**
         * Sets {@link #playerOnly}
         *
         * @param   playerOnly      the new {@link #playerOnly}
         *
         * @return                  the {@link MessageKeys} instance for chaining
         */
        @NotNull
        public MessageKeys playerOnly(@NotNull String playerOnly) {
            this.playerOnly = playerOnly;
            return this;
        }

        /**
         * Sets {@link #invalidArgument}
         *
         * @param   invalidArgument the new {@link #invalidArgument}
         *
         * @return                  the {@link MessageKeys} instance for chaining
         */
        @NotNull
        public MessageKeys invalidArgument(@NotNull String invalidArgument) {
            this.invalidArgument = invalidArgument;
            return this;
        }

        /**
         * Sets {@link #invalidArguments}
         *
         * @param   invalidArguments the new {@link #invalidArguments}
         *
         * @return                  the {@link MessageKeys} instance for chaining
         */
        @NotNull
        public MessageKeys invalidArguments(@NotNull String invalidArguments) {
            this.invalidArguments = invalidArguments;
            return this;
        }

        /**
         * Sets {@link #disabledCommand}
         *
         * @param   disabledCommand the new {@link #disabledCommand}
         *
         * @return                  the {@link MessageKeys} instance for chaining
         */
        @NotNull
        public MessageKeys disabledCommand(@NotNull String disabledCommand) {
            this.disabledCommand = disabledCommand;
            return this;
        }
    }
}
