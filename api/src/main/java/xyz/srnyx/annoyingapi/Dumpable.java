package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.NotNull;


/**
 * Represents an object that can be dumped to an object
 *
 * @param   <T> the type of object to dump to
 */
public interface Dumpable<T> {
    /**
     * Dumps the object to an object
     *
     * @param   to  the object to dump to
     *
     * @return      the dumped object
     */
    @NotNull
    T dump(@NotNull T to);
}
