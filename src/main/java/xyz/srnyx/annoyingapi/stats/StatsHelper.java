package xyz.srnyx.annoyingapi.stats;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.message.AnnoyingMessages;
import xyz.srnyx.annoyingapi.parents.Annoyable;


public class StatsHelper implements Annoyable {
    @NotNull private final AnnoyingPlugin plugin;

    public StatsHelper(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    @Nullable
    public String getStorageMethodName() {
        return plugin.dataManager == null ? null : plugin.dataManager.storageConfig.method.name();
    }

    @Nullable
    public Boolean getStorageCacheEnabled() {
        return plugin.dataManager == null ? null : plugin.dataManager.storageConfig.cache.enabled;
    }

    public @NotNull String @Nullable [] getStorageCacheSaveOn() {
        return plugin.dataManager == null ? null : plugin.dataManager.storageConfig.cache.getSaveOn().stream()
                .map(Enum::name)
                .toArray(String[]::new);
    }

    @Nullable
    public Long getStorageCacheInterval() {
        return plugin.dataManager == null ? null : plugin.dataManager.storageConfig.cache.interval.toMillis();
    }

    //TODO
//    @Nullable
//    public Integer getStorageConfiguredTableCount() {
//        return plugin.dataManager == null ? null : plugin.dataManager.dialect
//    }

    @NotNull
    public String[] getMessagesPluginGlobalPlaceholdersKeys() {
        final AnnoyingMessages msgs = plugin.getAnnoyingMessages();
        return msgs.plugin.global_placeholders.keySet().toArray(new String[0]);
    }

    @Nullable
    public String getMessagesPluginSplittersJson() {
        return plugin.getAnnoyingMessages().plugin.splitters.json;
    }

    @Nullable
    public String getMessagesPluginSplittersPlaceholder() {
        return plugin.getAnnoyingMessages().plugin.splitters.placeholder;
    }

    @Nullable
    public String getPlaceholderAPIVersion() {
        return plugin.papiInstalled ? PlaceholderAPIPlugin.getInstance().getDescription().getVersion() : null;
    }

    @Nullable
    public String getUpdateCheckerOutdatedLatestVersion() {
        return plugin.updateChecker != null && plugin.updateChecker.latestVersion != null && plugin.updateChecker.isUpdateAvailable() ? plugin.updateChecker.latestVersion.toString() : null;
    }
}
