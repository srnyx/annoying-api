package xyz.srnyx.annoyingexample;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingMessage;
import xyz.srnyx.annoyingapi.events.PlayerDamageByPlayerEvent;


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
    public ExampleListener(@NotNull ExamplePlugin plugin) {
        this.plugin = plugin;
    }

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
    public void onPlayerDamageByPlayer(@NotNull PlayerDamageByPlayerEvent event) {
        new AnnoyingMessage(plugin, "attack")
                .replace("%damager%", event.getDamager().getName())
                .replace("%damagee%", event.getDamagee().getName())
                .replace("%damage%", event.getDamage())
                .broadcast(AnnoyingMessage.BroadcastType.CHAT);
    }
}
