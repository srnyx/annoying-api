package xyz.srnyx.annoyingapi;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * A class that registers commands using the Brigadier API
 * <p><b>All</b> credit for this class goes to <a href="https://spigotmc.org/resources/authors/623700">realEntity303</a>, author of <a href="https://spigotmc.org/resources/88135">PlugManX</a>
 */
public class AnnoyingCommandRegister {
    private String nmsVersion;
    private Class<?> minecraftServerClass;
    private Method getServerMethod;
    private Field vanillaCommandDispatcherField;
    private Constructor<?> bukkitcommandWrapperConstructor;
    private Method registerMethod;
    private Method syncCommandsMethod;
    private Method aMethod;

    /**
     * Initialize the command register
     */
    public AnnoyingCommandRegister() {
        // nmsVersion
        try {
            this.nmsVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (final ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        // minecraftServerClass
        try {
            this.minecraftServerClass = Class.forName("net.minecraft.server." + this.nmsVersion + ".MinecraftServer");
        } catch (final ClassNotFoundException e) {
            try {
                this.minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            } catch (final ClassNotFoundException e2) {
                e2.addSuppressed(e);
                e2.printStackTrace();
            }
        }

        // getServerMethod
        try {
            this.getServerMethod = this.minecraftServerClass.getMethod("getServer");
            this.getServerMethod.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        }

        // vanillaCommandDispatcherField
        try {
            this.vanillaCommandDispatcherField = this.minecraftServerClass.getDeclaredField("vanillaCommandDispatcher");
            this.vanillaCommandDispatcherField.setAccessible(true);
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
        }

        // bukkitcommandWrapperConstructor
        try {
            this.bukkitcommandWrapperConstructor = Class.forName("org.bukkit.craftbukkit." + this.nmsVersion + ".command.BukkitCommandWrapper").getDeclaredConstructor(Class.forName("org.bukkit.craftbukkit." + this.nmsVersion + ".CraftServer"), Command.class);
            this.bukkitcommandWrapperConstructor.setAccessible(true);
        } catch (final NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // registerMethod
        try {
            this.registerMethod = Class.forName("org.bukkit.craftbukkit." + this.nmsVersion + ".command.BukkitCommandWrapper").getMethod("register", com.mojang.brigadier.CommandDispatcher.class, String.class);
            this.registerMethod.setAccessible(true);
        } catch (final NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // syncCommandsMethod
        try {
            this.syncCommandsMethod = Class.forName("org.bukkit.craftbukkit." + this.nmsVersion + ".CraftServer").getDeclaredMethod("syncCommands");
            this.syncCommandsMethod.setAccessible(true);
        } catch (final NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a command from another plugin
     *
     * @param   command         the command to register
     * @param   fallbackPrefix  the fallback command prefix
     */
    public void register(@NotNull Command command, @NotNull String fallbackPrefix) {
        if (nmsVersion == null) return;

        // Get commandDispatcher
        final Object commandDispatcher;
        try {
            commandDispatcher = this.vanillaCommandDispatcherField.get(this.getServerMethod.invoke(this.minecraftServerClass));
        } catch (final IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }

        // Get aMethod
        if (this.aMethod == null) try {
            this.aMethod = commandDispatcher.getClass().getDeclaredMethod("a");
            this.aMethod.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        // Register command
        try {
            this.registerMethod.invoke(this.bukkitcommandWrapperConstructor.newInstance(Bukkit.getServer(), command), this.aMethod.invoke(commandDispatcher), fallbackPrefix);
        } catch (final IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the command list
     */
    public void sync() {
        try {
            this.syncCommandsMethod.invoke(Bukkit.getServer());
        } catch (final IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
