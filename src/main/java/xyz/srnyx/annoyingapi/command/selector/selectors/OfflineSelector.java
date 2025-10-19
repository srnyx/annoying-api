package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import java.util.ArrayList;
import java.util.List;


public class OfflineSelector implements Selector<OfflinePlayer> {
    @Override @NotNull
    public Class<OfflinePlayer> getType() {
        return OfflinePlayer.class;
    }

    @Override @NotNull
    public List<OfflinePlayer> expand(@NotNull AnnoyingSender sender) {
        final List<OfflinePlayer> players = new ArrayList<>();
        for (final OfflinePlayer player : Bukkit.getOfflinePlayers()) if (!player.isOnline()) players.add(player);
        return players;
    }
}
