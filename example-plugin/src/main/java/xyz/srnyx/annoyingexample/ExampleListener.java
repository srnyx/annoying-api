package xyz.srnyx.annoyingexample;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingMessage;


/**
 * Example of a {@link AnnoyingListener} implementation
 */
public class ExampleListener implements AnnoyingListener {
    /**
     * {@link ExamplePlugin} instance
     */
    private final ExamplePlugin plugin;

    /**
     * Constructor for the {@link ExampleListener} class
     *
     * @param   plugin  the {@link ExamplePlugin} instance
     */
    @Contract(pure = true)
    public ExampleListener(@NotNull ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the plugin instance
     *
     * @return  the {@link ExamplePlugin} instance
     */
    @Override @NotNull
    public ExamplePlugin getPlugin() {
        return plugin;
    }

    /**
     * Called when a player joins a server
     *
     * @param   event   the {@link PlayerJoinEvent} event
     */
    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        new AnnoyingMessage(plugin, "join").send(event.getPlayer());
    }
}
