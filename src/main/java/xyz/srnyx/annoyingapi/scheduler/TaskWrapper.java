package xyz.srnyx.annoyingapi.scheduler;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;


/**
 * A wrapper for Bukkit and Folia tasks
 */
public class TaskWrapper {
    /**
     * The task object
     */
    @Nullable public Object task;
    /**
     * The {@link Type} of task
     */
    @Nullable public Type type;

    /**
     * Create an empty {@link TaskWrapper}
     * <br>Use {@link #setTask(Object)} once you have the task
     */
    public TaskWrapper() {}

    /**
     * Wrap a Bukkit or Folia task
     *
     * @param   task    {@link #task}
     */
    public TaskWrapper(@NotNull Object task) {
        setTask(task);
    }

    /**
     * Used internally to set {@link #task} and {@link #type} after creating an empty wrapper
     */
    void setTask(@NotNull Object task) {
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
        if (task == null) throw new IllegalStateException("Task not set yet!");
        if (type != Type.BUKKIT) throw new IllegalStateException("Task is not a Bukkit task!");
        return (BukkitTask) task;
    }

    /**
     * Cancel the task
     */
    public void cancel() {
        if (task == null) return;

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
        asBukkitTask().cancel();
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
