package xyz.srnyx.annoyingapi.message;

import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.validator.annotation.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.okaeri.RootConfig;
import xyz.srnyx.annoyingapi.file.okaeri.SubConfig;
import xyz.srnyx.annoyingapi.message.json.message.JsonChatMessage;
import xyz.srnyx.annoyingapi.message.json.message.JsonTitleMessage;
import xyz.srnyx.javautilities.MapGenerator;

import java.util.Map;


// Can't use @Header (@Include in consumer plugins doesn't include headers).
// Put in top key's @Comment instead.
public class AnnoyingMessages extends RootConfig {
    @Comment("DOCUMENTATION: https://annoying-api.srnyx.com/wiki/messages-file")
    @Comment
    @Comment
    @Comment("Messages for general plugin usage")
    @NotNull public Plugin plugin;

    @Comment
    @Comment("Error messages when a player does something wrong")
    @NotNull public Error error;


    @org.jetbrains.annotations.NotNull private transient final AnnoyingPlugin annoyingPlugin;
    @org.jetbrains.annotations.NotNull private transient final String defaultPrefix;
    @org.jetbrains.annotations.NotNull private transient final String defaultP;
    @org.jetbrains.annotations.NotNull private transient final String defaultS;

    public AnnoyingMessages(@org.jetbrains.annotations.NotNull AnnoyingPlugin annoyingPlugin) {
        this.annoyingPlugin = annoyingPlugin;
        this.defaultPrefix = annoyingPlugin.getMessages().defaults.prefix;
        this.defaultP = annoyingPlugin.getMessages().defaults.p;
        this.defaultS = annoyingPlugin.getMessages().defaults.s;

        this.plugin = new Plugin(this);
        this.error = new Error(this);
    }

    @org.jetbrains.annotations.NotNull
    public JsonChatMessage defaultMessage(@org.jetbrains.annotations.NotNull String raw) {
        return new JsonChatMessage(annoyingPlugin, raw);
    }

    @org.jetbrains.annotations.NotNull
    public JsonChatMessage defaultMessage(@org.jetbrains.annotations.NotNull Map<String, String> components) {
        return new JsonChatMessage(annoyingPlugin, components);
    }

    @org.jetbrains.annotations.NotNull
    public JsonTitleMessage defaultTitle(@org.jetbrains.annotations.NotNull String title, @org.jetbrains.annotations.NotNull String subtitle) {
        return new JsonTitleMessage(annoyingPlugin, title, subtitle);
    }

    public static class Plugin extends SubConfig<AnnoyingMessages> {
        public Plugin(@org.jetbrains.annotations.NotNull AnnoyingMessages root) {
            super(root);
        }

        @Comment("These are placeholders that can be used in any message in this file")
        @Comment("This is extremely useful for things like prefixes, color schemes, and more")
        @Comment("Using a global placeholder is just like any other placeholder! Simply surround the placeholder name with \"%\" (ex: \"%prefix%\")")
        @Comment("WARNING: Global placeholders can conflict with local placeholders! Please be wary when creating your own global placeholder(s)!")
        @Comment("It's recommended to keep all the default global placeholders (prefix, p, s, pe, se)")
        @NotNull public Map<String, String> global_placeholders = MapGenerator.LINKED_HASH_MAP.mapOf(
                "prefix", getRoot().defaultPrefix,
                "p", getRoot().defaultP,
                "s", getRoot().defaultS,
                "pe", "&c",
                "se", "&4");

        @Comment
        @Comment("These are the different splitters for messages/placeholders")
        @NotNull public Splitters splitters = new Splitters(this);

        @Comment
        @Comment("Message sent in the console when an update for the plugin is available")
        @Comment("Placeholders: %plugin%, %current%, %new%")
        @NotNull public JsonChatMessage update_available = getRoot().defaultMessage("%pe%A new version of %se%%plugin%%pe% is available! | Current: %se%%current%%pe% | Latest: %se%%new%");

        public static class Splitters extends SubConfig<Plugin> {
            public Splitters(@org.jetbrains.annotations.NotNull Plugin root) {
                super(root);
            }

            @Comment("This is the splitter for the JSON components. Default: \"@@\"")
            @NotNull public String json = "@@";

            @Comment("This is the splitter for placeholders with parameters. Default: \"==\"")
            @NotNull public String placeholder = "==";
        }
    }

    public static class Error extends SubConfig<AnnoyingMessages> {
        public Error(@org.jetbrains.annotations.NotNull AnnoyingMessages root) {
            super(root);
        }

        @Comment("Player doesn't have permission to use a command")
        @Comment("Placeholders: %permission%")
        @NotNull public JsonChatMessage no_permission = getRoot().defaultMessage("%prefix%%pe%You must have %se%%permission%%pe% to use this!@@%pe%%command%@@%command%");

        @Comment("Console tries to use a command that can only be used by players")
        @NotNull public JsonChatMessage player_only = getRoot().defaultMessage("%prefix%%pe%You must be a player to run this command!@@%pe%%command%@@%command%");

        @Comment("Command is used with an invalid/incorrect argument")
        @Comment("Placeholders: %argument%")
        @NotNull public JsonChatMessage invalid_argument = getRoot().defaultMessage("%prefix%%se%%argument%%pe% is an invalid argument!@@%pe%%command%@@%command%");

        @Comment("Command is used with multiple invalid/incorrect arguments")
        @NotNull public JsonChatMessage invalid_arguments = getRoot().defaultMessage("%prefix%%pe%Invalid arguments!@@%pe%%command%@@%command%");

        @Comment("Command is used when it's disabled")
        @NotNull public JsonChatMessage disabled_command = getRoot().defaultMessage("%prefix%%se%%command%%pe% is disabled!@@%pe%%command%@@%command%");
    }
}
