package xyz.srnyx.annoyingapi.plugin;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingCommand;
import xyz.srnyx.annoyingapi.AnnoyingMessage;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.AnnoyingSender;

import java.util.Collections;
import java.util.List;


public class ApiCommand implements AnnoyingCommand {
    private final AnnoyingPlugin plugin;

    @Contract(pure = true)
    public ApiCommand(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getPlugin() {
        return plugin;
    }

    @Override @NotNull
    public String getName() {
        return "annoying";
    }

    @Override @NotNull
    public String getPermission() {
        return "annoyingapi.command";
    }

    @Override
    public void onCommand(@NotNull AnnoyingSender sender) {
        new AnnoyingMessage(plugin, "version")
                .replace("%version%", plugin.getDescription().getVersion())
                .send(sender);
    }

    @Override
    public List<String> onTabComplete(@NotNull AnnoyingSender sender) {
        return Collections.singletonList("version");
    }
}
