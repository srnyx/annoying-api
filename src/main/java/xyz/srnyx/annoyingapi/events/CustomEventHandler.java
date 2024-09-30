package xyz.srnyx.annoyingapi.events;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Registrable;


/**
 * Convenience class for creating handlers for custom events
 */
@Registrable.Ignore
public class CustomEventHandler extends AnnoyingListener {
    /**
     * The {@link AnnoyingPlugin} that this handler is for
     */
    @NotNull protected final AnnoyingPlugin plugin;

    /**
     * Constructs a new handler for custom events
     *
     * @param   plugin  {@link #plugin}
     */
    public CustomEventHandler(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }
}
