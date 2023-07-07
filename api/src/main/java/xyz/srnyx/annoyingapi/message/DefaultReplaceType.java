package xyz.srnyx.annoyingapi.message;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.utility.AnnoyingUtility;

import java.util.function.BinaryOperator;


/**
 * Default replace types for {@link AnnoyingMessage#replace(String, Object, ReplaceType)}
 */
public enum DefaultReplaceType implements ReplaceType {
    /**
     * Input is used as the format for {@link AnnoyingUtility#formatMillis(long, String, boolean)}
     */
    TIME("mm':'ss", (input, value) -> {
        try {
            return AnnoyingUtility.formatMillis(Long.parseLong(value), input, false);
        } catch (final NumberFormatException e) {
            return null;
        }
    }),
    /**
     * Input is used as the format for {@link AnnoyingUtility#formatNumber(Number, String)}
     */
    NUMBER("#,###.##", (input, value) -> {
        try {
            return AnnoyingUtility.formatNumber(Double.parseDouble(value), input);
        } catch (final NumberFormatException e) {
            return null;
        }
    }),
    /**
     * Input is used to turn 'true' or 'false' into the specified value
     */
    BOOLEAN("true//false", (input, value) -> {
        final String[] split = input.split("//", 2);
        final boolean bool = Boolean.parseBoolean(value);
        if (split.length != 2) return bool ? "true" : "false";
        return bool ? split[0] : split[1];
    });

    /**
     * The default input for this {@link ReplaceType}
     */
    @NotNull private final String defaultInput;
    /**
     * The {@link BinaryOperator <String>} for this {@link ReplaceType}
     */
    @NotNull private final BinaryOperator<String> outputOperator;

    /**
     * Constructs a new {@link xyz.srnyx.annoyingapi.message.DefaultReplaceType}
     *
     * @param defaultInput   the default input value
     * @param outputOperator the {@link BinaryOperator<String>} to use on the input and value
     */
    DefaultReplaceType(@NotNull String defaultInput, @NotNull BinaryOperator<String> outputOperator) {
        this.defaultInput = defaultInput;
        this.outputOperator = outputOperator;
    }

    /**
     * Gets the default input of the type
     * <p>This is used if the user does not provide a custom input
     *
     * @return the default input
     */
    @Override @NotNull
    public String getDefaultInput() {
        return defaultInput;
    }

    /**
     * Gets the {@link BinaryOperator} to use on the input and value
     *
     * @return the {@link BinaryOperator} to use on the input and value
     */
    @Override @NotNull
    public BinaryOperator<String> getOutputOperator() {
        return outputOperator;
    }
}
