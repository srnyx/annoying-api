package xyz.srnyx.annoyingapi.command;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingMessage;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;


/**
 * Represents a command that is disabled by the API
 */
public class DisabledCommand implements AnnoyingCommand {
    /**
     * The {@link AnnoyingPlugin} instance
     */
    @NotNull private final AnnoyingPlugin plugin;

    /**
     * Constructor for {@link DisabledCommand}
     *
     * @param   plugin  the {@link AnnoyingPlugin} instance
     */
    @Contract(pure = true)
    public DisabledCommand(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * The {@link AnnoyingPlugin} that this command belongs to
     *
     * @return  the plugin instance
     */
    @Override @NotNull
    public AnnoyingPlugin getPlugin() {
        return plugin;
    }

    /**
     * <i>{@code REQUIRED}</i> This is everything that's executed when the command is run
     *
     * @param   sender  the sender of the command
     */
    @Override
    public void onCommand(@NotNull AnnoyingSender sender) {
        new AnnoyingMessage(plugin, plugin.options.disabledCommand).send(sender);
    }
}
