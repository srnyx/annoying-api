package xyz.srnyx.annoyingapi.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingMessage;
import xyz.srnyx.annoyingapi.AnnoyingOptions;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;


/**
 * This class is typically used in conjunction with {@link AnnoyingCommand}
 */
public class AnnoyingSender {
    @NotNull private final AnnoyingPlugin plugin;
    @NotNull private final CommandSender cmdSender;
    @Nullable private Command cmd;
    @Nullable private String label;
    @Nullable private String[] args;

    /**
     * Constructs a new {@link AnnoyingSender}
     *
     * @param   plugin      the {@link AnnoyingPlugin} instance
     * @param   cmdSender   the {@link CommandSender} to be used
     * @param   cmd         the {@link Command} that was used
     * @param   label       the {@link Command}'s label that was used
     * @param   args        the {@link Command}'s arguments that were used
     */
    @Contract(pure = true)
    public AnnoyingSender(@NotNull AnnoyingPlugin plugin, @NotNull CommandSender cmdSender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        this.plugin = plugin;
        this.cmdSender = cmdSender;
        this.cmd = cmd;
        this.label = label;
        this.args = args;
    }

    /**
     * Constructs a new {@link AnnoyingSender} without a {@link Command}
     *
     * @param   plugin      the {@link AnnoyingPlugin} instance
     * @param   cmdSender   the {@link CommandSender} to be used
     */
    @Contract(pure = true)
    public AnnoyingSender(@NotNull AnnoyingPlugin plugin, @NotNull CommandSender cmdSender) {
        this.plugin = plugin;
        this.cmdSender = cmdSender;
    }

    /**
     * Returns the {@link CommandSender} that was used
     *
     * @return  the {@link CommandSender} used
     */
    @NotNull
    public CommandSender getCmdSender() {
        return cmdSender;
    }

    /**
     * Returns the {@link Command} that was used
     *
     * @return  the {@link Command} that was used
     */
    @Nullable
    public Command getCmd() {
        return cmd;
    }

    /**
     * Returns the {@link Command}'s label that was used
     *
     * @return  the {@link Command}'s label that was used
     */
    @Nullable
    public String getLabel() {
        return label;
    }

    /**
     * Returns the {@link Command}'s arguments that were used
     *
     * @return  the {@link Command}'s arguments that were used
     */
    @Nullable
    public String[] getArgs() {
        return args;
    }

    /**
     * Casts the {@link CommandSender} to a {@link Player}
     * <p>Only use this if you know that the {@link CommandSender} is a {@link Player}
     *
     * @return  the {@link Player} that was used
     */
    @NotNull
    public Player getPlayer() {
        return (Player) cmdSender;
    }

    /**
     * Returns if the specified argument index is equal to the specified string (case-insensitive)
     *
     * @param   index   the argument index
     * @param   string  the string to compare to
     *
     * @return          if the specified argument index is equal to the specified string (case-insensitive)
     */
    public boolean argEquals(int index, @Nullable String string) {
        return args[index].equalsIgnoreCase(string);
    }

    /**
     * Checks if the {@link CommandSender} is a {@link Player}. If they aren't, it sends the {@link AnnoyingOptions#playerOnly} message
     *
     * @return  whether the {@link CommandSender} is a {@link Player}
     */
    public boolean checkPlayer() {
        final boolean isPlayer = cmdSender instanceof Player;
        if (!isPlayer) new AnnoyingMessage(plugin, plugin.options.playerOnly).send(this);
        return isPlayer;
    }

    /**
     * Checks if the {@link CommandSender} has the specified permission. If they don't, it sends the {@link AnnoyingOptions#noPermission} message
     *
     * @param   permission  the permission to check
     *
     * @return              if the {@link CommandSender} has the specified permission
     */
    public boolean checkPermission(@NotNull String permission) {
        final boolean hasPermission = cmdSender.hasPermission(permission);
        if (!hasPermission) new AnnoyingMessage(plugin, plugin.options.noPermission)
                .replace("%permission%", permission)
                .send(this);
        return hasPermission;
    }
}
