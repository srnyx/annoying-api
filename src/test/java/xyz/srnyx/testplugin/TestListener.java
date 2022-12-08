package xyz.srnyx.testplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingMessage;


public class TestListener implements AnnoyingListener {
    private final TestPlugin plugin;

    @Contract(pure = true)
    public TestListener(@NotNull TestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public TestPlugin getPlugin() {
        return plugin;
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        new AnnoyingMessage(plugin, "join").send(event.getPlayer());
    }
}
