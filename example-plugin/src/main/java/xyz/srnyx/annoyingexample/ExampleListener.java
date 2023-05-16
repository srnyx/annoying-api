package xyz.srnyx.annoyingexample;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingMessage;
import xyz.srnyx.annoyingapi.events.PlayerDamageByPlayerEvent;
import xyz.srnyx.annoyingapi.utility.ItemDataUtility;


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
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (plugin.item != null) event.getPlayer().getInventory().addItem(new ItemDataUtility(plugin, plugin.item).set("example", "example").item);
    }

    /**
     * This event will fire when a player is finishing consuming an item (food, potion, milk bucket). If the ItemStack is modified the server will use the effects of the new item and not remove the original one from the player's inventory. If the event is cancelled the effect will not be applied and the item will not be removed from the player's inventory.
     *
     * @param   event   the {@link PlayerItemConsumeEvent} event
     */
    @EventHandler
    public void onPlayerItemConsume(@NotNull PlayerItemConsumeEvent event) {
        if ("example".equals(new ItemDataUtility(plugin, event.getItem()).get("example"))) new AnnoyingMessage(plugin, "consume")
                .replace("%player%", event.getPlayer().getName())
                .broadcast(AnnoyingMessage.BroadcastType.ACTIONBAR);
    }

    /**
     * This event is called when a {@link Player} damages another {@link Player}
     *
     * @param   event   the {@link PlayerDamageByPlayerEvent} event
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
