package xyz.srnyx.annoyingapi.parents;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;


/**
 * Represents a class that has a built-in reference to the {@link AnnoyingPlugin} instance
 * <br>Useful to simplify classes that want to implement {@link Annoyable}
 */
public class AnnoyableClass implements Annoyable {
    /**
     * The {@link AnnoyingPlugin} instance
     */
    @NotNull protected final AnnoyingPlugin annoyingPlugin;

    /**
     * Creates a new {@link AnnoyableClass} with the given plugin
     *
     * @param   annoyingPlugin  {@link #annoyingPlugin}
     */
    public AnnoyableClass(@NotNull AnnoyingPlugin annoyingPlugin) {
        this.annoyingPlugin = annoyingPlugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return annoyingPlugin;
    }
}
