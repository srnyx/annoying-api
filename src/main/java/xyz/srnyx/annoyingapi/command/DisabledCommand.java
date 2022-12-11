package xyz.srnyx.annoyingapi.command;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingMessage;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;


public class DisabledCommand implements AnnoyingCommand {
    @NotNull private final AnnoyingPlugin plugin;

    @Contract(pure = true)
    public DisabledCommand(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onCommand(@NotNull AnnoyingSender sender) {
        new AnnoyingMessage(plugin, plugin.options.disabledCommand).send(sender);
    }
}
