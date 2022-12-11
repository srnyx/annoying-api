package xyz.srnyx.annoyingapi.dependency;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;

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
    private boolean initialized = false;
    private Class<?> minecraftServerClass;
    private Method getServerMethod;
    private Field vanillaCommandDispatcherField;
    private Constructor<?> bukkitcommandWrapperConstructor;
    private Method registerMethod;
    private Method syncCommandsMethod;
    private Method aMethod;
    private Field bField;
    private Method removeCommandMethod;

    /**
     * Initialize the command register
     */
    public AnnoyingCommandRegister() {
        // nmsVersion
        final String nmsVersion;
        try {
            nmsVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (final ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }

        // minecraftServerClass
        try {
            this.minecraftServerClass = Class.forName("net.minecraft.server." + nmsVersion + ".MinecraftServer");
        } catch (final ClassNotFoundException e) {
            try {
                this.minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            } catch (final ClassNotFoundException e2) {
                e2.addSuppressed(e);
                e2.printStackTrace();
                return;
            }
        }

        // getServerMethod
        try {
            this.getServerMethod = this.minecraftServerClass.getMethod("getServer");
            this.getServerMethod.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        // vanillaCommandDispatcherField
        try {
            this.vanillaCommandDispatcherField = this.minecraftServerClass.getDeclaredField("vanillaCommandDispatcher");
            this.vanillaCommandDispatcherField.setAccessible(true);
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }

        // bukkitcommandWrapperConstructor
        try {
            this.bukkitcommandWrapperConstructor = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".command.BukkitCommandWrapper").getDeclaredConstructor(Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".CraftServer"), Command.class);
            this.bukkitcommandWrapperConstructor.setAccessible(true);
        } catch (final NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // registerMethod
        try {
            this.registerMethod = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".command.BukkitCommandWrapper").getMethod("register", com.mojang.brigadier.CommandDispatcher.class, String.class);
            this.registerMethod.setAccessible(true);
        } catch (final NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // syncCommandsMethod
        try {
            this.syncCommandsMethod = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".CraftServer").getDeclaredMethod("syncCommands");
            this.syncCommandsMethod.setAccessible(true);
        } catch (final NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // bField
        try {
            this.bField = Class.forName("net.minecraft.server." + nmsVersion + ".CommandDispatcher").getDeclaredField("b");
            this.bField.setAccessible(true);
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            try {
                this.bField = Class.forName("net.minecraft.commands.CommandDispatcher").getDeclaredField("g");
                this.bField.setAccessible(true);
            } catch (final NoSuchFieldException | ClassNotFoundException ex) {
                ex.addSuppressed(e);
                e.printStackTrace();
                return;
            }
        }

        // removeCommandMethod
        try {
            this.removeCommandMethod = RootCommandNode.class.getDeclaredMethod("removeCommand", String.class);
        } catch (final NoSuchMethodException | NoSuchMethodError e) {
            try {
                this.removeCommandMethod = CommandNode.class.getDeclaredMethod("removeCommand", String.class);
            } catch (final NoSuchMethodException | NoSuchMethodError ex) {
                ex.addSuppressed(e);
                e.printStackTrace();
                return;
            }
        }

        initialized = true;
    }

    /**
     * Register a command from another plugin
     *
     * @param   command the command to register
     * @param   plugin  the plugin that owns the command
     */
    public void register(@NotNull Command command, @NotNull Plugin plugin) {
        if (!initialized) return;

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
            this.registerMethod.invoke(this.bukkitcommandWrapperConstructor.newInstance(Bukkit.getServer(), command), this.aMethod.invoke(commandDispatcher), plugin.getName());
        } catch (final IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void unregister(@NotNull Command command) {
        if (initialized) try {
            this.removeCommandMethod.invoke(((CommandDispatcher) this.bField.get(this.vanillaCommandDispatcherField.get(this.getServerMethod.invoke(this.minecraftServerClass)))).getRoot(), command);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the command list
     */
    public void sync() {
        if (!initialized) return;

        try {
            this.syncCommandsMethod.invoke(Bukkit.getServer());
        } catch (final IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
