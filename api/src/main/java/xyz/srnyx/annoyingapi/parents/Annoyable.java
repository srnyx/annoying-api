package xyz.srnyx.annoyingapi.parents;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;


/**
 * Represents a class that should have a reference to the {@link AnnoyingPlugin} instance
 */
public interface Annoyable {
    /**
     * The {@link AnnoyingPlugin} instance
     *
     * @return  the plugin instance
     */
    @NotNull
    AnnoyingPlugin getAnnoyingPlugin();
}
