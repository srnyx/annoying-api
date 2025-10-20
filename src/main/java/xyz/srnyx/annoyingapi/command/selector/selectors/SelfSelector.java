package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import java.util.Collections;
import java.util.List;


/**
 * Selector that selects the command sender if they are a player
 */
public class SelfSelector implements Selector<Player> {
    @Override @NotNull
    public Class<Player> getType() {
        return Player.class;
    }

    @Override @Nullable
    public List<Player> expand(@NotNull AnnoyingSender sender) {
        final Player player = sender.getPlayerOrNull();
        return player == null ? null : Collections.singletonList(player);
    }

    /**
     * Constructor for SelfSelector
     */
    public SelfSelector() {
        // Only exists to give the constructor a Javadoc
    }
}
