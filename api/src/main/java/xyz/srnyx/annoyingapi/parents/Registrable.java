package xyz.srnyx.annoyingapi.parents;


public interface Registrable extends Annoyable {
    boolean isRegistered();

    void setRegistered(boolean registered);

    void register();

    void unregister();
}
