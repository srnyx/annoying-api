package xyz.srnyx.annoyingexample;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingMessage;


public class ExampleListener implements AnnoyingListener {
    private final ExamplePlugin plugin;

    @Contract(pure = true)
    public ExampleListener(@NotNull ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public ExamplePlugin getPlugin() {
        return plugin;
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        new AnnoyingMessage(plugin, "join").send(event.getPlayer());
    }
}
