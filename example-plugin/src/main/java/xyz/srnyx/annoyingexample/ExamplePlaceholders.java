package xyz.srnyx.annoyingexample;

import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingMessage;
import xyz.srnyx.annoyingapi.parents.AnnoyingPAPIExpansion;


/**
 * An example of a placeholder expansion using {@link AnnoyingPAPIExpansion}
 */
public class ExamplePlaceholders extends AnnoyingPAPIExpansion {
    @NotNull private final ExamplePlugin plugin;

    /**
     * Constructor for the ExamplePlaceholders class
     *
     * @param   plugin  the {@link ExamplePlugin plugin} instance
     */
    public ExamplePlaceholders(@NotNull ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public ExamplePlugin getAnnoyingPlugin() {
        return plugin;
    }

    @Override @NotNull
    public String getIdentifier() {
        return "example";
    }

    @Override @NotNull
    public String onPlaceholderRequest(@Nullable Player player, @NotNull String parameters) {
        return new AnnoyingMessage(plugin, "test").toString();
    }
}
