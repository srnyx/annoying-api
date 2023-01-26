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
    /**
     * The {@link AnnoyingPlugin} instance
     */
    @NotNull public final AnnoyingPlugin plugin;
    /**
     * The {@link CommandSender} that was used to initialize this {@link AnnoyingSender}
     */
    @NotNull public final CommandSender cmdSender;
    /**
     * The {@link Command} that was used
     */
    @Nullable public final Command cmd;
    /**
     * The {@link Command}'s label that was used
     */
    @Nullable public final String label;
    /**
     * The {@link Command}'s arguments that were used
     */
    @Nullable public final String[] args;
    /**
     * Whether the {@link #cmdSender} is a {@link Player}
     */
    public final boolean isPlayer;

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
    public AnnoyingSender(@NotNull AnnoyingPlugin plugin, @NotNull CommandSender cmdSender, @Nullable Command cmd, @Nullable String label, @Nullable String[] args) {
        this.plugin = plugin;
        this.cmdSender = cmdSender;
        this.cmd = cmd;
        this.label = label;
        this.args = args;
        this.isPlayer = cmdSender instanceof Player;
    }

    /**
     * Constructs a new {@link AnnoyingSender} without a {@link Command}
     *
     * @param   plugin      the {@link AnnoyingPlugin} instance
     * @param   cmdSender   the {@link CommandSender} to be used
     */
    @Contract(pure = true)
    public AnnoyingSender(@NotNull AnnoyingPlugin plugin, @NotNull CommandSender cmdSender) {
        this(plugin, cmdSender, null, null, null);
    }

    /**
     * Casts the {@link CommandSender} to a {@link Player}
     * <p>Only use this if you know that the {@link CommandSender} is a {@link Player}
     *
     * @return  the {@link Player} that was used
     */
    @NotNull
    public Player getPlayer() {
        if (!isPlayer) throw new IllegalStateException("CommandSender is not a Player");
        return (Player) cmdSender;
    }

    /**
     * Returns if the specified {@link #args} index is equal to <b>any</b> of the specified strings (case-insensitive)
     *
     * @param   index   the argument index
     * @param   strings the strings to compare to
     *
     * @return          {@code true} if the specified {@link #args} index is equal to <b>any</b> of the specified strings (case-insensitive)
     */
    public boolean argEquals(int index, @Nullable String... strings) {
        final String arg = args[index];
        for (final String string : strings) if (arg.equalsIgnoreCase(string)) return true;
        return false;
    }

    /**
     * Checks if the {@link CommandSender} is a {@link Player}. If they aren't, it sends the {@link AnnoyingOptions#playerOnly} message
     *
     * @return  whether the {@link CommandSender} is a {@link Player}
     */
    public boolean checkPlayer() {
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
