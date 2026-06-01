package xyz.srnyx.annoyingapi.stats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
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

    @Nullable
    public String[] getStorageCacheSaveOn() {
        return plugin.dataManager == null ? null : plugin.dataManager.storageConfig.cache.saveOn.stream()
                .map(Enum::name)
                .toArray(String[]::new);
    }

    @Nullable
    public Long getStorageCacheInterval() {
        return plugin.dataManager == null ? null : plugin.dataManager.storageConfig.cache.interval;
    }

//    @Nullable
//    public Integer getStorageConfiguredTableCount() {
//        return plugin.dataManager == null ? null : plugin.dataManager.dialect
//    }

    @NotNull
    public String[] getMessagesPluginGlobalPlaceholdersKeys() {
        return plugin.globalPlaceholders.keySet().toArray(new String[0]);
    }

    @Nullable
    public String getMessagesPluginSplittersJson() {
        return plugin.messages == null ? null : plugin.messages.getString(plugin.options.messagesOptions.keys.splitterJson);
    }

    @Nullable
    public String getMessagesPluginSplittersPlaceholder() {
        return plugin.messages == null ? null : plugin.messages.getString(plugin.options.messagesOptions.keys.splitterPlaceholder);
    }
}
