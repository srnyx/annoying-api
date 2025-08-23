package xyz.srnyx.annoyingapi;

import org.bukkit.scheduler.BukkitTask;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;


/**
 * A wrapper for Bukkit and Folia tasks
 */
public class TaskWrapper {
    /**
     * The task object
     */
    @NotNull public final Object task;
    /**
     * The type of task
     */
    @NotNull public final Type type;

    /**
     * Wrap a Bukkit or Folia task
     *
     * @param   task    {@link #task}
     */
    public TaskWrapper(@NotNull Object task) {
        this.task = task;
        this.type = task instanceof BukkitTask ? Type.BUKKIT : Type.FOLIA;
    }

    /**
     * Return the task as a {@link BukkitTask}
     *
     * @return  {@link #task the task} as a {@link BukkitTask}
     */
    @NotNull
    public BukkitTask asBukkitTask() {
        return (BukkitTask) task;
    }

    /**
     * Cancel the task
     */
    public void cancel() {
        // Folia
        if (type == Type.FOLIA) {
            try {
                Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask").getMethod("cancel").invoke(task);
            } catch (final ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                AnnoyingPlugin.log(Level.SEVERE, "Failed to cancel a Folia task!", e);
            }
            return;
        }

        // Bukkit
        ((BukkitTask) task).cancel();
    }

    /**
     * Types of valid tasks for {@link TaskWrapper}
     */
    public enum Type {
        /**
         * A {@link BukkitTask}
         */
        BUKKIT,
        /**
         * A <a href="https://jd.papermc.io/folia/io/papermc/paper/threadedregions/scheduler/ScheduledTask.html">Folia task</a>
         */
        FOLIA
    }
}
