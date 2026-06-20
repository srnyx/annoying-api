package xyz.srnyx.annoyingapi.message;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.okaeri.SubConfig;
import xyz.srnyx.annoyingapi.message.json.JsonMessage;

import java.util.Map;


@Header("DOCUMENTATION: https://annoying-api.srnyx.com/wiki/messages-file")
public class AnnoyingMessages extends OkaeriConfig {
    @NotNull private transient final AnnoyingPlugin annoyingPlugin;

    public AnnoyingMessages(@NotNull AnnoyingPlugin annoyingPlugin) {
        this.annoyingPlugin = annoyingPlugin;
    }

    @NotNull
    public JsonMessage defaultMessage(@NotNull String raw) {
        return new JsonMessage(annoyingPlugin, raw);
    }

    @Comment
    @Comment
    @Comment("Messages for general plugin usage")
    @NotNull public Plugin plugin = new Plugin();

    @Comment
    @Comment("Error messages when a player does something wrong")
    @NotNull public Error error = new Error();

    public static class Plugin extends SubConfig<AnnoyingMessages> {
        @Comment("These are placeholders that can be used in any message in this file")
        @Comment("This is extremely useful for things like prefixes, color schemes, and more")
        @Comment("Using a global placeholder is just like any other placeholder! Simply surround the placeholder name with \"%\" (ex: \"%prefix%\")")
        @Comment("WARNING: Global placeholders can conflict with local placeholders! Please be wary when creating your own global placeholder(s)!")
        @Comment("It's recommended to keep all the default global placeholders (prefix, p, s, pe, se)")
        @NotNull public Map<String, String> global_placeholders = Map.of(
                "prefix", "&3&lANNOYING &8&l| &b",
                "p", "&b",
                "s", "&3",
                "pe", "&c",
                "se", "&4");

        @Comment
        @Comment("These are the different splitters for messages/placeholders")
        @NotNull public Splitters splitters = new Splitters();

        @Comment
        @Comment("Message sent in the console when an update for the plugin is available")
        @Comment("Placeholders: %plugin%, %current%, %new%")
        @NotNull public JsonMessage update_available = getRootConfig().defaultMessage("%pe%A new version of %se%%plugin%%pe% is available! | Current: %se%%current%%pe% | Latest: %se%%new%");

        public static class Splitters extends SubConfig<Plugin> {
            @Comment("This is the splitter for the JSON components. Default: \"@@\"")
            @NotNull public String json = "@@";

            @Comment("This is the splitter for placeholders with parameters. Default: \"==\"")
            @NotNull public String placeholder = "==";
        }
    }

    public static class Error extends SubConfig<AnnoyingMessages> {
        @Comment("Player doesn't have permission to use a command")
        @Comment("Placeholders: %permission%")
        @NotNull public JsonMessage no_permission = getRootConfig().defaultMessage("%prefix%%pe%You must have %se%%permission%%pe% to use this!@@%pe%%command%@@%command%");

        @Comment("Console tries to use a command that can only be used by players")
        @NotNull public JsonMessage player_only = getRootConfig().defaultMessage("%prefix%%pe%You must be a player to run this command!@@%pe%%command%@@%command%");

        @Comment("Command is used with an invalid/incorrect argument")
        @Comment("Placeholders: %argument%")
        @NotNull public JsonMessage invalid_argument = getRootConfig().defaultMessage("%prefix%%se%%argument%%pe% is an invalid argument!@@%pe%%command%@@%command%");

        @Comment("Command is used with multiple invalid/incorrect arguments")
        @NotNull public JsonMessage invalid_arguments = getRootConfig().defaultMessage("%prefix%%pe%Invalid arguments!@@%pe%%command%@@%command%");

        @Comment("Command is used when it's disabled")
        @NotNull public JsonMessage disabled_command = getRootConfig().defaultMessage("%prefix%%se%%command%%pe% is disabled!@@%pe%%command%@@%command%");
    }
}
