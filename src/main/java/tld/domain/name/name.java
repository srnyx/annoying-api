package tld.domain.name;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;


public class name extends JavaPlugin {
    /**
     * Called when this plugin is enabled
     */
    @Override
    public void onEnable() {
        // Start messages
        final Logger logger = getLogger();
        final String name = getName() + " v" + getDescription().getVersion();
        final String authors = "By " + String.join(", ", getDescription().getAuthors());
        final String line = "-".repeat(Math.max(name.length(), authors.length()));
        logger.info(ChatColor.DARK_AQUA + line);
        logger.info(ChatColor.AQUA + name);
        logger.info(ChatColor.AQUA + authors);
        logger.info(ChatColor.DARK_AQUA + line);
    }
}
