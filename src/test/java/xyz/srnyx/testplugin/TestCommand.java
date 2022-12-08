package xyz.srnyx.testplugin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingCommand;
import xyz.srnyx.annoyingapi.AnnoyingCooldown;
import xyz.srnyx.annoyingapi.AnnoyingMessage;
import xyz.srnyx.annoyingapi.AnnoyingSender;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class TestCommand implements AnnoyingCommand {
    private final TestPlugin plugin;

    @Contract(pure = true)
    public TestCommand(@NotNull TestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public TestPlugin getPlugin() {
        return plugin;
    }

    @Override @NotNull
    public String getName() {
        return "test";
    }

    @Override @NotNull
    public String getPermission() {
        return "test.command";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override @NotNull
    public Predicate<String[]> getArgsPredicate() {
        return args -> args.length == 1;
    }

    @Override
    public void onCommand(@NotNull AnnoyingSender sender) {
        final CommandSender cmdSender = sender.getCmdSender();
        if (!(cmdSender instanceof Player)) return;
        final Player player = (Player) cmdSender;

        // Check cooldown
        final AnnoyingCooldown cooldown = new AnnoyingCooldown(plugin, player.getUniqueId(), TestCooldown.TEST);
        if (cooldown.isOnCooldown()) {
            new AnnoyingMessage(plugin, "cooldown")
                    .replace("%cooldown%", cooldown.getRemaining(), AnnoyingMessage.ReplaceType.TIME)
                    .send(sender);
            return;
        }

        new AnnoyingMessage(plugin, "test").send(sender);
        cooldown.start();
    }

    @Override
    public List<String> onTabComplete(@NotNull AnnoyingSender sender) {
        final List<String> suggestions = new ArrayList<>();
        suggestions.add("test");
        suggestions.add("tab");
        return suggestions;
    }
}
