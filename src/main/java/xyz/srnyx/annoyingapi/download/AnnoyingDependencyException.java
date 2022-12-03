package xyz.srnyx.annoyingapi.download;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * This exception is thrown when a dependency is missing
 */
public class AnnoyingDependencyException extends Exception {
    /**
     * The dependency that is missing
     */
    private final transient AnnoyingDependency dependency;

    /**
     * Constructs a new {@link AnnoyingDependencyException} with the specified dependency
     *
     * @param   dependency  the dependency that is missing
     */
    public AnnoyingDependencyException(@NotNull AnnoyingDependency dependency) {
        super("Dependency " + dependency + " is missing!");
        this.dependency = dependency;
    }

    /**
     * Downloads the dependency that is missing
     *
     * @param   finish  the action to perform when the download is finished
     */
    public void download(@Nullable AnnoyingDownloadFinish finish) {
        AnnoyingPlugin.log(Level.WARNING, "&eDownloading required dependency: &6" + dependency.name() + "&e...");
        new AnnoyingDownload(dependency).downloadPlugins(finish);
    }

    /**
     * Downloads all dependencies that are missing
     *
     * @param   finish  the action to perform when all the downloads are finished
     */
    public void downloadAll(@Nullable AnnoyingDownloadFinish finish) {
        AnnoyingPlugin.log(Level.WARNING, "&eDownloading &6all&e dependencies...");
        new AnnoyingDownload(AnnoyingPlugin.OPTIONS.dependencies.stream()
                .filter(dep -> !dep.isInstalled())
                .collect(Collectors.toSet())).downloadPlugins(finish);
    }
}
