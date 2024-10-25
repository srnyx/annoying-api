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

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.logging.Level;


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
        if (arg == null) return false;
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
     * Gets the argument at the specified index
     *
     * @param   index   the argument index
     *
     * @return          the argument at the specified index
     */
    @Nullable
    public String getArgument(int index) {
        return args == null || args.length <= index ? null : args[index];
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
     * Gets the argument at the specified index as an {@link Optional}
     *
     * @param   index   the argument index
     *
     * @return          the argument at the specified index as an {@link Optional}
     */
    @NotNull
    public Optional<String> getArgumentOptional(int index) {
        return Optional.ofNullable(getArgument(index));
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
     * Gets multiple arguments joined together starting from the specified index and ending at the specified index (if too high, it will stop at the last argument)
     * <br>If no arguments are found, it returns an empty string
     *
     * @param   start   the starting index
     * @param   end     the ending index
     *
     * @return          the arguments joined together
     */
    @NotNull
    public String getArgumentsJoined(int start, int end) {
        if (args == null || args.length <= start) return "";
        final StringJoiner joiner = new StringJoiner(" ");
        for (int i = start; i < args.length && i < end; i++) joiner.add(args[i]);
        return joiner.toString();
    }

    /**
     * Gets multiple arguments joined together starting from the specified index
     * <br>If no arguments are found, it returns an empty string
     *
     * @param   start   the starting index
     *
     * @return          the arguments joined together
     */
    @NotNull
    public String getArgumentsJoined(int start) {
        return args == null ? "" : getArgumentsJoined(start, args.length);
    }

    /**
     * Gets all arguments joined together
     * <br>If no arguments are found, it returns an empty string
     *
     * @return  all arguments joined together
     */
    @NotNull
    public String getArgumentsJoined() {
        return args == null ? "" : String.join(" ", args);
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
