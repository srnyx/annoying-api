package xyz.srnyx.annoyingapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.jetbrains.annotations.NotNull;


/**
 * An extension of {@link PlaceholderExpansion} to allow for easier creation of PlaceholderAPI expansions
 */
public abstract class AnnoyingPAPIExpansion extends PlaceholderExpansion {
    /**
     * The {@link AnnoyingPlugin plugin} that this expansion is for
     *
     * @return  the {@link AnnoyingPlugin plugin} that this expansion is for
     */
    @NotNull
    public abstract AnnoyingPlugin getAnnoyingPlugin();

    @Override @NotNull
    public String getAuthor() {
        return getAnnoyingPlugin().getDescription().getAuthors().get(0);
    }

    @Override @NotNull
    public String getVersion() {
        return getAnnoyingPlugin().getDescription().getVersion();
    }

    @Override
    public final boolean persist() {
        return true;
    }
}
