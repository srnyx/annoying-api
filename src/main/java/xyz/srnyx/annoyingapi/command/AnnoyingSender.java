package xyz.srnyx.annoyingapi.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.selector.SelectorOptional;
import xyz.srnyx.annoyingapi.message.AnnoyingMessage;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.options.MessagesOptions;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import xyz.srnyx.javautilities.objects.Arguments;

import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;


/**
 * This class is typically used in conjunction with {@link AnnoyingCommand}
 */
public class AnnoyingSender extends Arguments implements Annoyable {
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
        super(args);
        this.plugin = plugin;
        this.cmdSender = cmdSender;
        this.command = command;
        this.label = label;
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
     * Gets the full/raw command that was used with the arguments (without {@code /})
     * <br>Example: {@code command arg1 arg2 arg3}
     *
     * @return  the full/raw command that was used with the arguments
     */
    @NotNull
    public String getFullCommand() {
        final StringBuilder command = new StringBuilder();
        if (label != null) command.append(label);
        if (args != null && args.length != 0) command.append(" ").append(getArgumentsJoined());
        return command.toString();
    }

    /**
     * Checks if the provided {@link CommandSender} is the same as the {@link #cmdSender}
     *
     * @param   sender  the {@link CommandSender} to check
     *
     * @return          if the provided {@link CommandSender} is the same as the {@link #cmdSender}
     */
    public boolean equalsSender(@NotNull CommandSender sender) {
        return cmdSender.equals(sender);
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
     * <p>Only use this if you know that the {@link CommandSender} is a {@link Player}, otherwise use {@link #getPlayerOrNull()}
     *
     * @return                          the {@link Player} that was used
     *
     * @throws  IllegalStateException   if the {@link CommandSender} is not a {@link Player}
     *
     * @see                             #getPlayerOrNull()
     */
    @NotNull
    public Player getPlayer() {
        if (!isPlayer) throw new IllegalStateException("CommandSender is not a Player");
        return (Player) cmdSender;
    }

    /**
     * If the {@link CommandSender} is a {@link Player}, it returns it as one. Otherwise, it returns {@code null}
     * <br>If you know that the {@link CommandSender} is a {@link Player}, use {@link #getPlayer()} instead
     *
     * @return  the {@link Player}, or {@code null} if the {@link CommandSender} is not a {@link Player}
     *
     * @see     #getPlayer()
     */
    @Nullable
    public Player getPlayerOrNull() {
        return isPlayer ? (Player) cmdSender : null;
    }

    /**
     * Returns the {@link Player} as an {@link Optional}, empty if the {@link CommandSender} is not a {@link Player}
     * <br>If you know that the {@link CommandSender} is a {@link Player}, use {@link #getPlayer()} instead
     *
     * @return  the {@link Player} as an {@link Optional}
     *
     * @see     #getPlayer()
     */
    @NotNull
    public Optional<Player> getPlayerOptional() {
        return Optional.ofNullable(getPlayerOrNull());
    }

    /**
     * Gets the argument at the specified index after applying the specified function
     * <br>If it's {@code null} before/after the function, send the invalid argument message
     * <br><b>Example usage:</b>
     * <pre>{@code
     * final Player target = sender.getArgument(2, Bukkit::getPlayer);
     * if (target == null) return;
     * }</pre>
     *
     * @param   index       the argument index
     * @param   function    the function to apply to the argument
     *
     * @return              the argument at the specified index after applying the specified function
     *
     * @param   <T>         the type of the argument
     */
    @Nullable
    public <T> T getArgument(int index, @NotNull Function<String, T> function) {
        final String argument = getArgument(index);
        if (argument == null) {
            invalidArgumentByIndex(index);
            return null;
        }
        final T value = function.apply(argument);
        if (value == null) invalidArgument(argument);
        return value;
    }

    /**
     * Gets the argument at the specified index as an {@link Optional} after applying the specified function
     * <br>If it's {@link Optional#empty() empty} before/after the function, send the invalid argument message
     * <br><b>Example usage:</b>
     * <pre>{@code
     * final Optional<Player> target = sender.getArgument(2, Bukkit::getPlayer);
     * if (!target.isPresent()) return;
     * }</pre>
     *
     * @param   index       the argument index
     * @param   function    the function to apply to the argument
     *
     * @return              the argument at the specified index as an {@link Optional} after applying the specified function
     *
     * @param   <T>         the type of the argument
     */
    @NotNull
    public <T> Optional<T> getArgumentOptional(int index, @NotNull Function<String, T> function) {
        final Optional<T> optional = getArgumentOptional(index).map(function);
        if (!optional.isPresent()) invalidArgumentByIndex(index);
        return optional;
    }

    /**
     * Gets the argument at the specified index as an {@link Optional} after applying the specified function and flattening it
     * <br>If it's {@link Optional#empty() empty} before/after the function, send the invalid argument message
     * <br><b>Example usage:</b>
     * <pre>{@code
     * final Optional<OfflinePlayer> target = sender.getArgumentFlat(2, BukkitUtility::getOfflinePlayer);
     * if (!target.isPresent()) return;
     * }</pre>
     *
     * @param   index       the argument index
     * @param   function    the function to apply to the argument
     *
     * @return              the argument at the specified index as an {@link Optional} after applying the specified function and flattening it
     *
     * @param   <T>         the type of the argument
     */
    @NotNull
    public <T> Optional<T> getArgumentOptionalFlat(int index, @NotNull Function<String, Optional<T>> function) {
        final Optional<T> optional = getArgumentOptional(index).flatMap(function);
        if (!optional.isPresent()) invalidArgumentByIndex(index);
        return optional;
    }

    /**
     * Gets the selector at the specified index as a {@link SelectorOptional}
     *
     * @param   index   the argument index
     * @param   type    the selector type
     *
     * @return          the selector at the specified index as a {@link SelectorOptional}
     *
     * @param   <T>     the type of the selector
     */
    @NotNull
    public <T> SelectorOptional<T> getSelector(int index, @NotNull Class<T> type) {
        final String argument = getArgument(index);
        if (argument == null) {
            invalidArguments();
            return SelectorOptional.noArgument(this);
        }
        final SelectorOptional<T> selector = SelectorOptional.of(this, argument, type);
        if (selector.isEmpty()) invalidArgument(argument);
        return selector;
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
     * <br>If {@link #args} is {@code null} or the specified index is out of bounds, it logs a warning to console
     *
     * @param   index   the argument index
     */
    public void invalidArgumentByIndex(int index) {
        final boolean invalid = args == null || args.length <= index;
        if (invalid) AnnoyingPlugin.log(Level.WARNING, "&4[" + label + "]&c Invalid argument index for invalidArgumentByIndex: &4" + index);
        invalidArgument(invalid ? index : args[index]);
    }

    /**
     * Sends the invalid arguments message
     */
    public void invalidArguments() {
        new AnnoyingMessage(plugin, plugin.options.messagesOptions.keys.invalidArguments).send(this);
    }
}
