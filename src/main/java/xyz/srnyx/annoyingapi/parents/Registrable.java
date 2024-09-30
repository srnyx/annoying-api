package xyz.srnyx.annoyingapi.parents;

import xyz.srnyx.annoyingapi.options.RegistrationOptions;
import xyz.srnyx.javautilities.parents.Stringable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * An abstract class for classes that can be registered/unregistered by the plugin
 */
public abstract class Registrable extends Stringable implements Annoyable {
    /**
     * Constructs a new {@link Registrable} for registration
     */
    public Registrable() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * Returns whether the class is registered or not
     *
     * @return whether the class is registered or not
     */
    public boolean isRegistered() {
        return getAnnoyingPlugin().registeredClasses.contains(this);
    }

    /**
     * Sets whether the class is registered or not
     *
     * @param   registered  whether the class is registered or not
     */
    public void setRegistered(boolean registered) {
        if (registered) {
            register();
            return;
        }
        unregister();
    }

    /**
     * Registers the class
     */
    public void register() {
        getAnnoyingPlugin().registeredClasses.add(this);
    }

    /**
     * Unregisters the class
     */
    public void unregister() {
        getAnnoyingPlugin().registeredClasses.remove(this);
    }

    /**
     * An annotation to ignore a class from being automatically registered via {@link RegistrationOptions.AutomaticRegistration}
     *
     * @see RegistrationOptions.AutomaticRegistration#ignoredClasses
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ignore {}
}
