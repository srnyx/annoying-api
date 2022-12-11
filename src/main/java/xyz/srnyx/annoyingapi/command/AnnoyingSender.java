package xyz.srnyx.annoyingapi.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * This class is typically used in conjunction with {@link AnnoyingCommand}
 */
public class AnnoyingSender {
    @NotNull private final CommandSender cmdSender;
    @Nullable private Command cmd;
    @Nullable private String label;
    @Nullable private String[] args;

    /**
     * Constructs a new {@link AnnoyingSender}
     *
     * @param   cmdSender   the {@link CommandSender} to be used
     * @param   cmd         the {@link Command} that was used
     * @param   label       the {@link Command}'s label that was used
     * @param   args        the {@link Command}'s arguments that were used
     */
    @Contract(pure = true)
    public AnnoyingSender(@NotNull CommandSender cmdSender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        this.cmdSender = cmdSender;
        this.cmd = cmd;
        this.label = label;
        this.args = args;
    }

    /**
     * Constructs a new {@link AnnoyingSender} without a {@link Command}
     *
     * @param   cmdSender   the {@link CommandSender} to be used
     */
    @Contract(pure = true)
    public AnnoyingSender(@NotNull CommandSender cmdSender) {
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
}
