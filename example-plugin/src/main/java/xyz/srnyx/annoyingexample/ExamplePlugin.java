package xyz.srnyx.annoyingexample;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
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
        options.registrationOptions(registrationOptions -> registrationOptions
                .commandsToRegister(new ExampleCommand(this))
                .listenersToRegister(new ExampleListener(this))
                .papiExpansionToRegister(new ExamplePlaceholders(this)));
    }

    @Override
    public void enable() {
        // Recipe YML example
        final AnnoyingResource config = new AnnoyingResource(this, "config.yml");
        this.item = config.getItemStack("recipe.result");
        final Recipe recipe = config.getRecipe("recipe", null);
        if (recipe != null) Bukkit.addRecipe(recipe);
        this.sound = config.getPlayableSound("sound");

        // Data example
        final AnnoyingData data = new AnnoyingData(this, "data.yml");
        data.set("super.cool.test", 105);
        data.setSave("hello", "world!");
    }
}
