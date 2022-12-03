package xyz.srnyx.annoyingapi.download;

import org.jetbrains.annotations.NotNull;

import java.util.Set;


/**
 * Used to handle the download finish event
 */
public interface AnnoyingDownloadFinish {
    /**
     * This is called when all dependencies have been downloaded
     *
     * @param   plugins the {@link Set} of {@link AnnoyingDependency}s that were processed
     */
    void onFinish(@NotNull Set<AnnoyingDependency> plugins);
}
