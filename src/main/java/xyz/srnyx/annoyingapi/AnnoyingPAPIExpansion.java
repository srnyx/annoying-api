package xyz.srnyx.annoyingapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.parents.Annoyable;


/**
 * An extension of {@link PlaceholderExpansion} to allow for easier creation of PlaceholderAPI expansions
 */
public abstract class AnnoyingPAPIExpansion extends PlaceholderExpansion implements Annoyable {
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

    /**
     * Constructor for the {@link AnnoyingPAPIExpansion} class
     */
    public AnnoyingPAPIExpansion() {
        // Only exists to give the constructor a Javadoc
    }
}
