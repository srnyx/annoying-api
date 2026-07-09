package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.OkaeriConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @param   <R> The top-level root config
 * @param   <P> The defaults parent
 */
public class SubConfig<R extends OkaeriConfig, P extends OkaeriConfig> extends AnnoyingConfig {
    /**
     * This only exists for defaults!
     */
    @Nullable private final P defaultsParent;

    /**
     * Okaeri Configs will do an "unsafe" initialization with default constructor (no args) for non-default value
     */
    public SubConfig(@NotNull P defaultsParent) {
        this.defaultsParent = defaultsParent;
    }

    /**
     * This is only non-null for defaults
     */
    @NotNull
    public P getParent() {
        if (defaultsParent == null) throw new IllegalStateException("getDefaultsParent() called in a non-defaults context");
        return defaultsParent;
    }

    @NotNull
    public R getRoot() {
        // Defaults context
        if (defaultsParent != null) {
            OkaeriConfig current = this;
            while (current instanceof SubConfig<?, ?> subConfig) current = subConfig.getParent();
            return (R) current;
        }

        // Non-defaults context
        return (R) getContext().getRootConfig();
    }

    @Override @NotNull
    public SubConfig<R, P> save() {
        getRoot().save();
        return this;
    }
}
