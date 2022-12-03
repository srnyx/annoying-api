package xyz.srnyx.annoyingapi.message;

import org.jetbrains.annotations.Contract;


/**
 * This class is used to create a title message
 */
public class AnnoyingTitle {
    /**
     * How long the text will take to fade in (in ticks)
     */
    private final int fadeIn;
    /**
     * How long the text will stay on the screen (in ticks)
     */
    private final int stay;
    /**
     * How long the text will take to fade out (in ticks)
     */
    private final int fadeOut;

    /**
     * Constructs a new {@link AnnoyingTitle} with the specified fade in, stay, and fade out times
     *
     * @param   fadeIn  how long the text will take to fade in (in ticks)
     * @param   stay    how long the text will stay on the screen (in ticks)
     * @param   fadeOut how long the text will take to fade out (in ticks)
     */
    @Contract(pure = true)
    public AnnoyingTitle(int fadeIn, int stay, int fadeOut) {
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    /**
     * Constructs a new {@link AnnoyingTitle} with the default fade in, stay, and fade out times (20, 20, 20)
     */
    @Contract(pure = true)
    public AnnoyingTitle() {
        this(20, 20, 20);
    }

    /**
     * @return  how long the text will take to fade in (in ticks)
     */
    public int getFadeIn() {
        return fadeIn;
    }

    /**
     * @return  how long the text will stay on the screen (in ticks)
     */
    public int getStay() {
        return stay;
    }

    /**
     * @return  how long the text will take to fade out (in ticks)
     */
    public int getFadeOut() {
        return fadeOut;
    }
}
