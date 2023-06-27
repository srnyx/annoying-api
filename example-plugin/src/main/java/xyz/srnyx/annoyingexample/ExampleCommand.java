package xyz.srnyx.annoyingexample;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingCommand;
import xyz.srnyx.annoyingapi.AnnoyingCooldown;
import xyz.srnyx.annoyingapi.AnnoyingMessage;
import xyz.srnyx.annoyingapi.command.AnnoyingSender;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;


/**
 * Example of a {@link AnnoyingCommand} implementation
 */
public class ExampleCommand implements AnnoyingCommand {
    /**
     * {@link ExamplePlugin} instance
     */
    private final ExamplePlugin plugin;

    /**
     * Constructor for the {@link ExampleCommand} class
     *
     * @param   plugin  the {@link ExamplePlugin} instance
     */
    public ExampleCommand(@NotNull ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public ExamplePlugin getAnnoyingPlugin() {
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
        final AnnoyingCooldown cooldown = new AnnoyingCooldown(plugin, sender.getPlayer().getUniqueId(), ExampleCooldown.EXAMPLE);
        if (cooldown.isOnCooldown()) {
            new AnnoyingMessage(plugin, "cooldown")
                    .replace("%cooldown%", cooldown.getRemaining(), AnnoyingMessage.DefaultReplaceType.TIME)
                    .send(sender);
            return;
        }
        cooldown.start();

        // test
        if (sender.argEquals(0, "test")) {
            new AnnoyingMessage(plugin, "test").send(sender);
            return;
        }

        // reload
        if (sender.argEquals(0, "reload")) {
            plugin.reloadPlugin();
            return;
        }

        // disable
        if (sender.argEquals(0, "disable")) {
            unregister();
            return;
        }

        sender.invalidArguments();
    }

    @Override @Nullable
    public Collection<String> onTabComplete(@NotNull AnnoyingSender sender) {
        return Arrays.asList("test", "reload", "disable");
    }
}
