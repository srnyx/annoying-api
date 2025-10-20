package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import java.util.ArrayList;
import java.util.List;


/**
 * Selector that selects all online players
 */
public class OnlineSelector implements Selector<Player> {
    @Override @NotNull
    public Class<Player> getType() {
        return Player.class;
    }

    @Override @NotNull
    public List<Player> expand(@NotNull AnnoyingSender sender) {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    /**
     * Constructor for OnlineSelector
     */
    public OnlineSelector() {
        // Only exists to give the constructor a Javadoc
    }
}
