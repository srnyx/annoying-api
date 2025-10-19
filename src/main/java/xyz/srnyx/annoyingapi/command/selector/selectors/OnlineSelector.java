package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import java.util.ArrayList;
import java.util.List;


public class OnlineSelector implements Selector<Player> {
    @Override @NotNull
    public Class<Player> getType() {
        return Player.class;
    }

    @Override @NotNull
    public List<Player> expand(@NotNull AnnoyingSender sender) {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }
}
