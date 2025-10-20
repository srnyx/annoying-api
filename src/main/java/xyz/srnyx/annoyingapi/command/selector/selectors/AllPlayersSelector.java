package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import java.util.Arrays;
import java.util.List;


/**
 * A selector that selects all players (online and offline)
 */
public class AllPlayersSelector implements Selector<OfflinePlayer> {
    @Override @NotNull
    public Class<OfflinePlayer> getType() {
        return OfflinePlayer.class;
    }

    @Override @NotNull
    public List<OfflinePlayer> expand(@NotNull AnnoyingSender sender) {
        return Arrays.asList(Bukkit.getOfflinePlayers());
    }

    /**
     * Constructor for AllPlayersSelector
     */
    public AllPlayersSelector() {
        // Only exists to give the constructor a Javadoc
    }
}
