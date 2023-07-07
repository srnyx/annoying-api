package xyz.srnyx.annoyingapi.options;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.PluginPlatform;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class PluginOptions {
    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingDependency AnnoyingDependencies} to check for (add dependencies to this in the plugin's constructor)
     * <p>If you add a dependency to this OUTSIDE the constructor, it will not be checked
     * <p><i>This is <b>NOT</b> meant for optional dependencies, all of these dependencies will be downloaded/installed (even if {@link AnnoyingDependency#required} is {@code false})</i>
     */
    @NotNull public List<AnnoyingDependency> dependencies = new ArrayList<>();
    /**
     * <i>{@code RECOMMENDED}</i> The different {@link PluginPlatform platforms} the plugin is available on
     * <p>If not specified, the plugin will not be able to check for updates
     */
    @NotNull public PluginPlatform.Multi updatePlatforms = new PluginPlatform.Multi();

    /**
     * Constructs a new {@link PluginOptions} instance with default values
     */
    public PluginOptions() {
        // Only exists to give the constructor a Javadoc
    }

    @NotNull
    public static PluginOptions load(@NotNull ConfigurationSection section) {
        final PluginOptions options = new PluginOptions();
        options.dependencies.addAll(AnnoyingDependency.loadList(section, "dependencies"));
        options.updatePlatforms = PluginPlatform.Multi.load(section, "updatePlatforms");
        return options;
    }

    /**
     * Adds the specified {@link AnnoyingDependency}s to {@link #dependencies}
     *
     * @param   dependencies    the dependencies to add
     *
     * @return                  the {@link PluginOptions} instance for chaining
     */
    @NotNull
    public PluginOptions dependencies(@NotNull Collection<AnnoyingDependency> dependencies) {
        this.dependencies.addAll(dependencies);
        return this;
    }

    /**
     * Adds the specified {@link AnnoyingDependency}s to {@link #dependencies}
     *
     * @param   dependencies    the dependencies to add
     *
     * @return                  the {@link PluginOptions} instance for chaining
     */
    @NotNull
    public PluginOptions dependencies(@NotNull AnnoyingDependency... dependencies) {
        return dependencies(Arrays.asList(dependencies));
    }

    /**
     * Sets {@link #updatePlatforms}
     *
     * @param   updatePlatforms     the new {@link #updatePlatforms}
     *
     * @return                      the {@link PluginOptions} instance for chaining
     */
    @NotNull
    public PluginOptions updatePlatforms(@NotNull PluginPlatform.Multi updatePlatforms) {
        this.updatePlatforms = updatePlatforms;
        return this;
    }
}
