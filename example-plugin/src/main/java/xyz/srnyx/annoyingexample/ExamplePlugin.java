package xyz.srnyx.annoyingexample;

import org.bukkit.ChatColor;

import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingData;

import java.util.EnumMap;
import java.util.Map;


/**
 * Example of a {@link AnnoyingPlugin} implementation
 */
public class ExamplePlugin extends AnnoyingPlugin {
    /**
     * Constructor for the {@link ExamplePlugin} class
     */
    public ExamplePlugin() {
        super();

        // Options
        options.colorLight = ChatColor.LIGHT_PURPLE;
        options.colorDark = ChatColor.DARK_PURPLE;
        options.messagesFileName = "msgs.yml";
        options.prefix = "prefix";
        options.splitterJson = "splitter.json";
        options.splitterPlaceholder = "splitter.placeholder";
        options.noPermission = "no-permission";
        options.playerOnly = "player-only";
        options.invalidArguments = "invalid-arguments";
        options.disabledCommand = "disabled-command";
        options.commands.add(new ExampleCommand(this));
        options.listeners.add(new ExampleListener(this));

        // Dependencies
        final Map<AnnoyingDownload.Platform, String> viaVersion = new EnumMap<>(AnnoyingDownload.Platform.class);
        viaVersion.put(AnnoyingDownload.Platform.SPIGOT, "19254");
        options.dependencies.add(new AnnoyingDependency("ViaVersion", viaVersion, true, true));
    }

    @Override
    public void enable() {
        final AnnoyingData data = new AnnoyingData(this, "data.yml");
        data.set("super.cool.test", 105);
        data.set("hello", "world!", true);
    }
}
