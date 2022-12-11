package xyz.srnyx.testplugin;

import org.bukkit.ChatColor;

import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingData;

import java.util.EnumMap;
import java.util.Map;


public class TestPlugin extends AnnoyingPlugin {
    public TestPlugin() {
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
        options.commands.add(new TestCommand(this));
        options.listeners.add(new TestListener(this));

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
