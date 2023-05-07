package xyz.srnyx.annoyingexample;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingData;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.utility.MapUtility;


/**
 * Example of a {@link AnnoyingPlugin} implementation
 */
public class ExamplePlugin extends AnnoyingPlugin {
    /**
     * Constructor for the {@link ExamplePlugin} class
     */
    public ExamplePlugin() {
        super();

        // Options
        options.bStatsId = 12345;
        options.bStatsFileName = "stats.yml";
        options.bStatsOptions = new AnnoyingResource.ResourceOptions().createDefaultFile(false);
        options.messagesFileName = "msgs.yml";
        options.globalPlaceholders = "placeholders";
        options.splitterJson = "splitter.json";
        options.splitterPlaceholder = "splitter.placeholder";
        options.noPermission = "no-permission";
        options.playerOnly = "player-only";
        options.invalidArguments = "invalid-arguments";
        options.disabledCommand = "disabled-command";
        options.commandsToRegister.add(new ExampleCommand(this));
        options.listenersToRegister.add(new ExampleListener(this));
        options.papiExpansionToRegister = () -> new ExamplePlaceholders(this);

        // Dependencies
        options.dependencies.add(new AnnoyingDependency("ViaVersion", MapUtility.mapOf(AnnoyingDownload.Platform.SPIGOT, "19254"), true, true));
    }

    @Override
    public void enable() {
        // Recipe YML example
        final Recipe recipe = new AnnoyingResource(this, "config.yml").getRecipe("recipe", null, new ItemStack(Material.GOLDEN_APPLE));
        if (recipe != null) Bukkit.addRecipe(recipe);

        // Data example
        final AnnoyingData data = new AnnoyingData(this, "data.yml");
        data.set("super.cool.test", 105);
        data.setSave("hello", "world!");
    }
}
