package xyz.srnyx.annoyingexample;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.PluginPlatform;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.file.AnnoyingData;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.file.PlayableSound;


/**
 * Example of a {@link AnnoyingPlugin} implementation
 */
public class ExamplePlugin extends AnnoyingPlugin {
    /**
     * Example {@link ItemStack item} generated from the {@code config.yml} file
     */
    @Nullable public ItemStack item;
    /**
     * Example {@link PlayableSound sound} generated from the {@code config.yml} file
     */
    @Nullable public PlayableSound sound;

    /**
     * Constructor for the {@link ExamplePlugin} class
     */
    public ExamplePlugin() {
        options
                .pluginOptions(pluginOptions -> pluginOptions.dependencies(new AnnoyingDependency(
                        "ViaVersion",
                        new PluginPlatform.Multi(
                                PluginPlatform.hangar("ViaVersion", "ViaVersion"),
                                PluginPlatform.spigot("19254")),
                        true, true)))
                .registrationOptions(registrationOptions -> registrationOptions
                        .automaticRegistration(automaticRegistration -> automaticRegistration.packages(
                                "xyz.srnyx.annoyingexample.commands",
                                "xyz.srnyx.annoyingexample.listeners"))
                        .papiExpansionToRegister(() -> new ExamplePlaceholders(this)))
                .bStatsOptions(bStatsOptions -> bStatsOptions
                        .id(12345)
                        .fileName("stats.yml")
                        .fileOptions(fileOptions -> fileOptions.createDefaultFile(false))
                        .toggleKey("enable-stats"))
                .messagesOptions(messagesOptions -> messagesOptions
                        .fileName("msgs.yml")
                        .keys(messageKeys -> messageKeys
                                .globalPlaceholders("placeholders")
                                .splitterJson("splitter.json")
                                .splitterPlaceholder("splitter.placeholder")
                                .noPermission("no-permission")
                                .playerOnly("player-only")
                                .invalidArguments("invalid-arguments")
                                .disabledCommand("disabled-command")));
    }

    @Override
    public void enable() {
        // Recipe & PlayableSound YML example
        final AnnoyingResource config = new AnnoyingResource(this, "config.yml");
        final Recipe recipe = config.getRecipe("recipe", null);
        if (recipe != null) {
            item = recipe.getResult();
            Bukkit.addRecipe(recipe);
        }
        sound = config.getPlayableSound("sound");

        // Data example
        final AnnoyingData data = new AnnoyingData(this, "data.yml");
        data.set("super.cool.test", 105);
        data.setSave("hello", "world!");
    }
}
