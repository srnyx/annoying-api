package xyz.srnyx.annoyingapi.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.message.AnnoyingMessage;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.options.MessagesOptions;
import xyz.srnyx.annoyingapi.parents.Annoyable;
import xyz.srnyx.annoyingapi.parents.Stringable;


/**
 * This class is typically used in conjunction with {@link AnnoyingCommand}
 */
public class AnnoyingSender extends Stringable implements Annoyable {
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
    @Nullable public final Command command;
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
     * @param   command     the {@link Command} that was used
     * @param   label       the {@link Command}'s label that was used
     * @param   args        the {@link Command}'s arguments that were used
     */
    public AnnoyingSender(@NotNull AnnoyingPlugin plugin, @NotNull CommandSender cmdSender, @Nullable Command command, @Nullable String label, @Nullable String[] args) {
        this.plugin = plugin;
        this.cmdSender = cmdSender;
        this.command = command;
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
    public AnnoyingSender(@NotNull AnnoyingPlugin plugin, @NotNull CommandSender cmdSender) {
        this(plugin, cmdSender, null, null, null);
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
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
        if (args == null || args.length <= index) return false;
        final String arg = args[index];
        for (final String string : strings) if (arg.equalsIgnoreCase(string)) return true;
        return false;
    }

    /**
     * Checks if the {@link CommandSender} is a {@link Player}. If they aren't, it sends the {@link MessagesOptions.MessageKeys#playerOnly} message
     *
     * @return  whether the {@link CommandSender} is a {@link Player}
     */
    public boolean checkPlayer() {
        if (!isPlayer) new AnnoyingMessage(plugin, plugin.options.messagesOptions.keys.playerOnly).send(this);
        return isPlayer;
    }

    /**
     * Checks if the {@link CommandSender} has the specified permission. If they don't, it sends the {@link MessagesOptions.MessageKeys#noPermission} message
     *
     * @param   permission  the permission to check
     *
     * @return              if the {@link CommandSender} has the specified permission
     */
    public boolean checkPermission(@NotNull String permission) {
        final boolean hasPermission = cmdSender.hasPermission(permission);
        if (!hasPermission) new AnnoyingMessage(plugin, plugin.options.messagesOptions.keys.noPermission)
                .replace("%permission%", permission)
                .send(this);
        return hasPermission;
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
     * Sends the invalid argument message, replacing {@code %argument%} with the specified argument
     *
     * @param   argument    the argument to replace {@code %argument%} with
     */
    public void invalidArgument(@Nullable Object argument) {
        new AnnoyingMessage(plugin, plugin.options.messagesOptions.keys.invalidArgument)
                .replace("%argument%", String.valueOf(argument))
                .send(this);
    }

    /**
     * Sends the invalid argument message, replacing {@code %argument%} with the specified argument
     *
     * @param   index   the argument index
     */
    public void invalidArgumentByIndex(int index) {
        invalidArgument(args == null || args.length <= index ? index : args[index]);
    }

    /**
     * Sends the invalid arguments message
     */
    public void invalidArguments() {
        new AnnoyingMessage(plugin, plugin.options.messagesOptions.keys.invalidArguments).send(this);
    }
}
