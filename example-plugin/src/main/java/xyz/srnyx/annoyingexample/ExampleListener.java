package xyz.srnyx.annoyingexample;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.message.AnnoyingMessage;
import xyz.srnyx.annoyingapi.events.PlayerDamageByPlayerEvent;
import xyz.srnyx.annoyingapi.message.BroadcastType;
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
    public ExamplePlugin getAnnoyingPlugin() {
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
     * Called when a block is broken by a player.
     * <p>If you wish to have the block drop experience, you must set the experience value above 0. By default, experience will be set in the event if:
     * <ol>
     *     <li>The player is not in creative or adventure mode
     *     <li>The player can loot the block (ie: does not destroy it completely, by using the correct tool)
     *     <li>The player does not have silk touch
     *     <li>The block drops experience in vanilla Minecraft
     * </ol>
     * <p>Note: Plugins wanting to simulate a traditional block drop should set the block to air and utilize their own methods for determining what the default drop for the block being broken is and what to do about it, if anything.
     * <p>If a Block Break event is cancelled, the block will not break and experience will not drop.
     *
     * @param   event   the {@link BlockBreakEvent} event
     */
    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = player.getItemInHand();
        if (!"example".equals(new ItemDataUtility(plugin, item).get("example"))) return;
        final short maxDurability = item.getType().getMaxDurability();
        new AnnoyingMessage(plugin, "break")
                .replace("%player%", player.getName())
                .replace("%durability%", maxDurability - item.getDurability() - 1 + "/" + maxDurability)
                .broadcast(BroadcastType.ACTIONBAR);
        if (plugin.sound != null) plugin.sound.play(event.getPlayer());
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
                .broadcast(BroadcastType.CHAT);
    }
}
