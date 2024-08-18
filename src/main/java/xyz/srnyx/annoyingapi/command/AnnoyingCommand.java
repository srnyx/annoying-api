package xyz.srnyx.annoyingapi.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.message.AnnoyingMessage;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Registrable;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * Represents a command that can be executed by a player or the console
 */
@SuppressWarnings("SameReturnValue")
public abstract class AnnoyingCommand extends Registrable implements TabExecutor {
    /**
     * Constructs a new command instance for registration
     */
    public AnnoyingCommand() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * <i>{@code OPTIONAL}</i> This is the name of the command
     * <p>If not specified, the lowercase class name will be used ({@code Command} will be removed)
     * <p><b>Example:</b> the command class {@code MyEpicCommand} would be registered as {@code myepic}
     *
     * @return  the name of the command
     */
    @NotNull
    public String getName() {
        return getClass().getSimpleName().replace("Command", "").replace("Cmd", "").toLowerCase();
    }

    /**
     * <i>{@code OPTIONAL}</i> This is the permission required to use the command
     * <p>If not specified (or {@code null}), no permission will be required
     *
     * @return  the permission required to use the command
     */
    @Nullable
    public String getPermission() {
        return null;
    }

    /**
     * <i>{@code OPTIONAL}</i> Whether the command is player-only (no console), default: {@code false}
     *
     * @return  whether the command is player-only
     */
    public boolean isPlayerOnly() {
        return false;
    }

    /**
     * <i>{@code OPTIONAL}</i> The command's arguments will be tested against this predicate
     * <p>This is usually used to check if the command has the correct amount of arguments
     * <p>If not specified, any arguments will be accepted
     *
     * @return  the predicate to test the command's arguments against
     */
    @NotNull
    public Predicate<String[]> getArgsPredicate() {
        return args -> true;
    }

    /**
     * Registers the command to the {@link #getAnnoyingPlugin()}
     */
    @Override
    public void register() {
        if (isRegistered()) return;
        final PluginCommand command = getAnnoyingPlugin().getCommand(getName());
        if (command == null) {
            AnnoyingPlugin.log(Level.WARNING, "&cCommand &4" + getName() + "&c not found in plugin.yml!");
            return;
        }
        command.setExecutor(this);
        super.register();
    }

    /**
     * Unregisters the command from the {@link #getAnnoyingPlugin()}
     */
    @Override
    public void unregister() {
        if (!isRegistered()) return;
        final PluginCommand command = getAnnoyingPlugin().getCommand(getName());
        if (command != null) command.setExecutor(new DisabledCommand(getAnnoyingPlugin()));
        super.unregister();
    }

    /**
     * <i>{@code REQUIRED}</i> This is everything that's executed when the command is run
     *
     * @param   sender  the sender of the command
     */
    public abstract void onCommand(@NotNull AnnoyingSender sender);

    /**
     * <i>{@code OPTIONAL}</i> This is the tab completion for the command
     * <p><i>{@link BukkitUtility} will come in handy</i>
     *
     * @param   sender  the sender of the command
     *
     * @return          a {@link Collection} of suggestions
     */
    @Nullable
    public Collection<String> onTabComplete(@NotNull AnnoyingSender sender) {
        return null;
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param   cmdSender   Source of the command
     * @param   cmd         Command which was executed
     * @param   label       Alias of the command which was used
     * @param   args        Passed command arguments
     * @return              true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender cmdSender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        final AnnoyingSender sender = new AnnoyingSender(getAnnoyingPlugin(), cmdSender, cmd, label, args);

        // Permission & player check
        final String permission = getPermission();
        if ((permission != null && !sender.checkPermission(permission)) || (isPlayerOnly() && !sender.checkPlayer())) return true;

        // Argument check
        if (!getArgsPredicate().test(args)) {
            new AnnoyingMessage(getAnnoyingPlugin(), getAnnoyingPlugin().options.messagesOptions.keys.invalidArguments).send(sender);
            return true;
        }

        // Run command
        onCommand(sender);
        return true;
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param   cmdSender   Source of the command. For players tab-completing a command inside a command block, this will be the player, not the command block.
     * @param   cmd         Command which was executed
     * @param   label       Alias of the command which was used
     * @param   args        The arguments passed to the command, including final partial argument to be completed
     * @return              A List of possible completions for the final argument, or null to default to the command executor
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender cmdSender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        // Permission check
        final String permission = getPermission();
        if (permission != null && !cmdSender.hasPermission(permission)) return Collections.emptyList();

        // Get suggestions
        final Collection<String> suggestions = onTabComplete(new AnnoyingSender(getAnnoyingPlugin(), cmdSender, cmd, label, args));
        if (suggestions == null) return Collections.emptyList();

        // Filter suggestions
        return suggestions.stream()
                .filter(string -> string.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
