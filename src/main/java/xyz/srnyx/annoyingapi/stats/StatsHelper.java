package xyz.srnyx.annoyingapi.stats;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.message.AnnoyingMessages;
import xyz.srnyx.annoyingapi.parents.AnnoyableClass;


public class StatsHelper extends AnnoyableClass {
    public StatsHelper(@NotNull AnnoyingPlugin plugin) {
        super(plugin);
    }

    @Nullable
    public String getStorageMethodName() {
        return annoyingPlugin.dataManager == null ? null : annoyingPlugin.dataManager.storageConfig.method.name();
    }

    @Nullable
    public Boolean getStorageCacheEnabled() {
        return annoyingPlugin.dataManager == null ? null : annoyingPlugin.dataManager.storageConfig.cache.enabled;
    }

    public @NotNull String @Nullable [] getStorageCacheSaveOn() {
        return annoyingPlugin.dataManager == null ? null : annoyingPlugin.dataManager.storageConfig.cache.getSaveOn().stream()
                .map(Enum::name)
                .toArray(String[]::new);
    }

    @Nullable
    public Long getStorageCacheInterval() {
        return annoyingPlugin.dataManager == null ? null : annoyingPlugin.dataManager.storageConfig.cache.interval.toMillis();
    }

    //TODO
//    @Nullable
//    public Integer getStorageConfiguredTableCount() {
//        return plugin.dataManager == null ? null : plugin.dataManager.dialect
//    }

    @NotNull
    public String[] getMessagesPluginGlobalPlaceholdersKeys() {
        final AnnoyingMessages msgs = annoyingPlugin.getAnnoyingMessages();
        return msgs.plugin.global_placeholders.keySet().toArray(new String[0]);
    }

    @Nullable
    public String getMessagesPluginSplittersJson() {
        return annoyingPlugin.getAnnoyingMessages().plugin.splitters.json;
    }

    @Nullable
    public String getMessagesPluginSplittersPlaceholder() {
        return annoyingPlugin.getAnnoyingMessages().plugin.splitters.placeholder;
    }

    @Nullable
    public String getPlaceholderAPIVersion() {
        return annoyingPlugin.papiInstalled ? PlaceholderAPIPlugin.getInstance().getDescription().getVersion() : null;
    }

    @Nullable
    public String getUpdateCheckerOutdatedLatestVersion() {
        return annoyingPlugin.updateChecker != null && annoyingPlugin.updateChecker.latestVersion != null && annoyingPlugin.updateChecker.isUpdateAvailable() ? annoyingPlugin.updateChecker.latestVersion.toString() : null;
    }
}
