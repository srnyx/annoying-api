package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.OkaeriConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class SubConfig<R extends OkaeriConfig> extends AnnoyingConfig {
    /**
     * This only exists for defaults!
     */
    @Nullable private final R root;

    /**
     * Okaeri Configs will do an "unsafe" initialization with default constructor (no args) for non-default value
     */
    public SubConfig(@NotNull R root) {
        this.root = root;
    }

    /**
     * Try the defaults-available root first, then the context root.
     * <br>At least one of them should be non-null!
     */
    @NotNull
    public R getRoot() {
        if (root != null) return root;
        return (R) getContext().getRootConfig();
    }

    @Override @NotNull
    public SubConfig<R> save() {
        getRoot().save();
        return this;
    }
}
