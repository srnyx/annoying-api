package xyz.srnyx.annoyingapi;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import xyz.srnyx.annoyingapi.events.AdvancedPlayerMoveEvent;
import xyz.srnyx.annoyingapi.parents.Registrable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * A listener that can be registered to the Bukkit event system
 */
public abstract class AnnoyingListener extends Registrable implements Listener {
    /**
     * Whether the custom events have already been registered
     */
    private boolean registeredCustomEvents = false;

    /**
     * Constructs a new listener instance for registration
     */
    public AnnoyingListener() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * Registers the listener to the {@link #getAnnoyingPlugin() plugin}
     * <br>If the listener contains any custom events from Annoying API (such as {@link AdvancedPlayerMoveEvent}), those events' respective listener handlers will be registered as well.
     * <br>It will only attempt to register those custom events once (when it's first registered), if this listener is unregistered and registered again, it will not attempt to register the custom events again.
     */
    @Override
    public void register() {
        if (isRegistered()) return;
        final AnnoyingPlugin plugin = getAnnoyingPlugin();

        // Only attempt to register custom events once
        if (!registeredCustomEvents) {
            // Get methods
            final Class<? extends AnnoyingListener> clazz = getClass();
            final Method[] methods = clazz.getMethods();
            final Method[] declaredMethods = clazz.getDeclaredMethods();
            final Set<Method> methodsSet = new HashSet<>(methods.length + declaredMethods.length);
            methodsSet.addAll(Arrays.asList(methods));
            methodsSet.addAll(Arrays.asList(declaredMethods));

            // Check for custom events
            for (final Method method : methodsSet) {
                if (!method.isAnnotationPresent(EventHandler.class)) continue;
                final Class<?>[] params = method.getParameterTypes();
                if (params.length == 0) continue;
                final AnnoyingListener listener = plugin.customEvents.get(params[0]);
                if (listener != null) listener.register();
            }
            registeredCustomEvents = true;
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        super.register();
    }

    /**
     * Unregisters the listener from the {@link #getAnnoyingPlugin() plugin}
     */
    @Override
    public void unregister() {
        if (!isRegistered()) return;
        HandlerList.unregisterAll(this);
        super.unregister();
    }
}
