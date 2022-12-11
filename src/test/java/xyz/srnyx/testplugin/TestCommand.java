package xyz.srnyx.testplugin;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.command.AnnoyingCommand;
import xyz.srnyx.annoyingapi.AnnoyingCooldown;
import xyz.srnyx.annoyingapi.AnnoyingMessage;
import xyz.srnyx.annoyingapi.command.AnnoyingSender;

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
        // Check cooldown
        final AnnoyingCooldown cooldown = new AnnoyingCooldown(plugin, sender.getPlayer().getUniqueId(), TestCooldown.TEST);
        if (cooldown.isOnCooldown()) {
            new AnnoyingMessage(plugin, "cooldown")
                    .replace("%cooldown%", cooldown.getRemaining(), AnnoyingMessage.ReplaceType.TIME)
                    .send(sender);
            return;
        }

        if (sender.argEquals(0, "test")) {
            new AnnoyingMessage(plugin, "test").send(sender);
        } else if (sender.argEquals(0, "reload")) {
            plugin.reload();
        }

        cooldown.start();
    }

    @Override
    public List<String> onTabComplete(@NotNull AnnoyingSender sender) {
        final List<String> suggestions = new ArrayList<>();
        suggestions.add("test");
        suggestions.add("reload");
        return suggestions;
    }
}
