package xyz.srnyx.annoyingapi.parents;


/**
 * An interface for classes that can be registered/unregistered by the plugin
 */
public interface Registrable extends Annoyable {
    /**
     * Returns whether the class is registered or not
     *
     * @return  whether the class is registered or not
     */
    boolean isRegistered();

    /**
     * Sets whether the class is registered or not
     *
     * @param   registered  whether the class is registered or not
     */
    void setRegistered(boolean registered);

    /**
     * Registers the class
     */
    void register();

    /**
     * Unregisters the class
     */
    void unregister();
}
