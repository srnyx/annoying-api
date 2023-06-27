package xyz.srnyx.annoyingapi.parents;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;


public interface Annoyable {
    /**
     * The {@link AnnoyingPlugin} instance
     *
     * @return  the plugin instance
     */
    @NotNull
    AnnoyingPlugin getAnnoyingPlugin();
}
