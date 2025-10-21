package xyz.srnyx.annoyingapi.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;


/**
 * A scheduler for running tasks on Bukkit or Folia
 */
public class AnnoyingScheduler implements Annoyable {
    @NotNull private final AnnoyingPlugin plugin;

    /**
     * Constructs a new {@link AnnoyingScheduler} instance
     *
     * @param   plugin  the {@link AnnoyingPlugin} this scheduler belongs to
     */
    public AnnoyingScheduler(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    /**
     * Attempt to run a task synchronously on the main server thread
     * <br>If running Folia, the task will be run on Folia's global scheduler (as Folia is inherently asynchronous)
     *
     * @param   runnable    the task to run
     *
     * @return              a {@link TaskWrapper} containing the scheduled task
     */
    @NotNull
    public TaskWrapper runSync(@NotNull Runnable runnable) {
        // Folia
        if (AnnoyingPlugin.FOLIA) {
            try {
                final Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                return new TaskWrapper(scheduler.getClass().getMethod("run", Plugin.class, Consumer.class).invoke(scheduler, plugin, new FoliaConsumer(runnable)));
            } catch (final InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Failed to run a Folia task!", e);
            }
        }

        // Bukkit
        return new TaskWrapper(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    /**
     * Attempt to run a task asynchronously
     * <br>If running Folia, the task will be run on Folia's global scheduler
     * <br>If the plugin is disabled, the task will be run synchronously on the current thread
     *
     * @param   runnable    the task to run
     *
     * @return              an {@link Optional} containing a {@link TaskWrapper} if the task was scheduled, or an empty {@link Optional} if the plugin is disabled
     */
    @NotNull @SuppressWarnings("UnusedReturnValue")
    public Optional<TaskWrapper> attemptAsync(@NotNull Runnable runnable) {
        try {
            return Optional.of(new TaskWrapper(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)));
        } catch (final IllegalPluginAccessException | UnsupportedOperationException e) {
            // UnsupportedOperationException: Server is using Folia
            if (e instanceof UnsupportedOperationException && AnnoyingPlugin.FOLIA) return Optional.of(runSync(runnable));
            // IllegalPluginAccessException: Plugin is disabled
            runnable.run();
            return Optional.empty();
        }
    }

    /**
     * Runs a global task at a later time
     * <br>If running Folia, the task will be run on Folia's global scheduler (as Folia is inherently asynchronous)
     *
     * @param   runnable    the task to run
     * @param   delay       tick delay before the task starts
     *
     * @return              a {@link TaskWrapper} containing the task and its {@link TaskWrapper.Type type}
     */
    @NotNull
    public TaskWrapper runGlobalTaskLater(@NotNull Runnable runnable, long delay) {
        // Folia
        if (AnnoyingPlugin.FOLIA) return runGlobalTaskLaterFolia(runnable, delay);
        // Bukkit
        return new TaskWrapper(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay));
    }

    /**
     * Runs a global task later asynchronously
     * <br>If running Folia, the task will be run on Folia's global scheduler (as Folia is inherently asynchronous)
     *
     * @param   runnable    the task to run
     * @param   delay       tick delay before the task starts
     *
     * @return              a {@link TaskWrapper} containing the task and its {@link TaskWrapper.Type type}
     */
    @NotNull
    public TaskWrapper runGlobalTaskLaterAsync(@NotNull Runnable runnable, long delay) {
        // Folia
        if (AnnoyingPlugin.FOLIA) return runGlobalTaskLaterFolia(runnable, delay);
        // Bukkit
        return new TaskWrapper(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    /**
     * Runs a global task later using Folia
     * <br><b>For internal use only, use {@link #runGlobalTaskLater(Runnable, long)} instead!</b>
     *
     * @param   runnable    the task to run
     * @param   delay       tick delay before the task starts
     *
     * @return              a {@link TaskWrapper} containing the task with {@link TaskWrapper.Type#FOLIA}
     */
    @NotNull
    private TaskWrapper runGlobalTaskLaterFolia(@NotNull Runnable runnable, long delay) {
        try {
            final Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            return new TaskWrapper(scheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class).invoke(scheduler, plugin, new FoliaConsumer(runnable), delay));
        } catch (final InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to run a Folia task!", e);
        }
    }

    /**
     * Runs a global task timer
     * <br>If running Folia, the task will be run on Folia's global scheduler (as Folia is inherently asynchronous)
     *
     * @param   runnable    the task to run
     * @param   delay       tick delay before the task starts
     * @param   interval    tick interval between each execution of the task
     *
     * @return              a {@link TaskWrapper} containing the task and its {@link TaskWrapper.Type type}
     */
    @NotNull @SuppressWarnings("UnusedReturnValue")
    public TaskWrapper runGlobalTaskTimer(@NotNull Runnable runnable, long delay, long interval) {
        // Folia
        if (AnnoyingPlugin.FOLIA) return runGlobalTaskTimerFolia(runnable, delay, interval);
        // Bukkit
        return new TaskWrapper(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, interval));
    }

    /**
     * Runs a global task timer asynchronously
     * <br>If running Folia, the task will be run on Folia's global scheduler (as Folia is inherently asynchronous)
     *
     * @param   runnable    the task to run
     * @param   delay       tick delay before the task starts
     * @param   interval    tick interval between each execution of the task
     *
     * @return              a {@link TaskWrapper} containing the task and its {@link TaskWrapper.Type type}
     */
    @NotNull @SuppressWarnings("UnusedReturnValue")
    public TaskWrapper runGlobalTaskTimerAsync(@NotNull Runnable runnable, long delay, long interval) {
        // Folia
        if (AnnoyingPlugin.FOLIA) return runGlobalTaskTimerFolia(runnable, delay, interval);
        // Bukkit
        return new TaskWrapper(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, interval));
    }

    /**
     * Runs a global task timer using Folia
     * <br><b>For internal use only, use {@link #runGlobalTaskTimer(Runnable, long, long)} instead!</b>
     *
     * @param   runnable    the task to run
     * @param   delay       tick delay before the task starts
     * @param   interval    tick interval between each execution of the task
     *
     * @return              a {@link TaskWrapper} containing the task with {@link TaskWrapper.Type#FOLIA}
     */
    @NotNull
    private TaskWrapper runGlobalTaskTimerFolia(@NotNull Runnable runnable, long delay, long interval) {
        try {
            final Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            return new TaskWrapper(scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class).invoke(scheduler, plugin, new FoliaConsumer(runnable), delay, interval));
        } catch (final InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to run a Folia task!", e);
        }
    }

    /**
     * Only used for {@link #runGlobalTaskTimerFolia(Runnable, long, long) Folia task timers} due to reflection
     */
    private static class FoliaConsumer implements Consumer<Object> {
        /**
         * The {@link Runnable} to run
         */
        @NotNull private final Runnable runnable;

        /**
         * Constructs a new {@link FoliaConsumer} instance
         *
         * @param   runnable    {@link #runnable}
         */
        public FoliaConsumer(@NotNull Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void accept(@NotNull Object object) {
            runnable.run();
        }
    }
}
