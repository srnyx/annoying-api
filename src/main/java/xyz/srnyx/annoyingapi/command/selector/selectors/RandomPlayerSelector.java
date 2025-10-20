package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;

import xyz.srnyx.annoyingapi.command.selector.Selector;
import xyz.srnyx.javautilities.MiscUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Selector that selects a random online player
 */
public class RandomPlayerSelector implements Selector<Player> {
    @Override @NotNull
    public Class<Player> getType() {
        return Player.class;
    }

    @Override @NotNull
    public List<Player> expand(@NotNull AnnoyingSender sender) {
        final List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        final int size = players.size();
        return size == 0 ? Collections.emptyList() : Collections.singletonList(players.get(MiscUtility.RANDOM.nextInt(size)));
    }

    /**
     * Constructor for RandomPlayerSelector
     */
    public RandomPlayerSelector() {
        // Only exists to give the constructor a Javadoc
    }
}
