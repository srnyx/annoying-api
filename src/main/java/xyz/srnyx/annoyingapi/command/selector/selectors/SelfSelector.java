package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Selector that selects the command sender if they are a player
 */
public class SelfSelector extends Selector<Player> {
    private static final Set<Class<? extends CommandSender>> ALLOWED_SENDERS = Collections.singleton(Player.class);

    @Override @NotNull
    public Class<Player> getType() {
        return Player.class;
    }

    @Override @NotNull
    public Set<Class<? extends CommandSender>> getAllowedSenders() {
        return ALLOWED_SENDERS;
    }

    @Override @Nullable
    public List<Player> expandImplementation(@NotNull AnnoyingSender sender) {
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
